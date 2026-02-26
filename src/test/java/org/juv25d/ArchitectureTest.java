package org.juv25d;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;

/**
 * This system follows a specific lifecycle where an HTTP request is processed through specialized layers.
 * To maintain a clean architecture, the core principle is that lower layers must never depend on higher layers.
 * While the flow is not strictly linear—for instance, filters must call the chain to proceed—these rules ensure that
 * dependencies only move in authorized directions and that components do not "skip" steps unnecessarily
 *
 * The request lifecycle is designed to follow this strict flow: (Runtime Flow)
 *
 * Client
 * ↓
 * ServerSocket
 * ↓
 * ConnectionHandler (Virtual Thread)
 * ↓
 * Pipeline
 * ↓
 * FilterChain
 * ↓
 * Router
 * ↓
 * Plugin
 * ↓
 * HttpResponseWriter
 * ↓
 * Client
 *
 * Note: This describes the runtime execution flow, not direct code dependencies.
 * Dependencies are allowed for bootstrapping and controlled object creation,
 * but must never violate the downward lifecycle direction.
 */

public class ArchitectureTest {

    private static JavaClasses importedClasses;


    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("org.juv25d");
    }
    /**
     *  This rule ensures that only the Server and its associated factories can initiate a ConnectionHandler.
     *  This prevents other parts of the application from accidentally manipulating direct client connections.
     */
    @Test
    void connectionHandlerAccessRule () {
        ArchRuleDefinition.classes()
            .that().haveSimpleName("ConnectionHandler")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("Server")
                    .or(simpleName("ConnectionHandler"))
                    .or(simpleName("DefaultConnectionHandlerFactory"))
                    .or(simpleName("ConnectionHandlerFactory")))
            .as("ConnectionHandler access rule")
            .because("ConnectionHandler should only be accessed by server, connectionhandler or its factories")
            .check(importedClasses);
    }

    /**
     * Only the network layer (ConnectionHandler) or the application's startup class (App) may interact with the Pipeline.
     * This guarantees that the execution chain remains intact and is not modified during an active request.
     */
    @Test
    void pipelineAccessRule () {
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Pipeline")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("ConnectionHandler")
                    .or(simpleName("ConnectionHandlerFactory"))
                    .or(simpleName("DefaultConnectionHandlerFactory"))
                    .or(simpleName("Pipeline"))
                    .or(simpleName("App")) // App handles bootstrapping and wiring of the Pipeline during startup. This should stay.
                    .or(simpleName("Bootstrap")))
            .as("Pipeline access rule")
            .because("Pipeline should only be accessed by ConnectionHandler, App, Bootstrap during setup")
            .check(importedClasses);
    }


    /**
     * The FilterChain is created by the Pipeline and triggered by the ConnectionHandler.
     * This rule also allows individual filters to access the chain.
     */
    @Test
    void filterChainRule () {
        ArchRuleDefinition.classes()
            .that().haveSimpleName("FilterChain")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("Pipeline")
                    .or(simpleName("FilterChain"))
                    .or(simpleName("FilterChainImpl"))
                    .or(resideInAPackage("..filter.."))
                    .or(simpleName("ConnectionHandler"))) //This needs to be accessed because ConnectionHandler creates doFilter()
            .as("FilterChain access rule")
            .because("FilterChain should only be accessed by Pipeline, ConnectionHandler")
            .check(importedClasses);
    }


    /**
     * The Router should only be accessed by the FilterChain to determine which plugin to execute,
     * or by App and Pipeline during the system's bootstrapping phase.
     */
    @Test
    void routerRule () {
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Router")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("FilterChain")
                    .or(simpleName("FilterChainImpl"))
                    .or(simpleName("Router"))
                    .or(simpleName("Pipeline")) //Pipeline injects router
                    .or(simpleName("App"))) //App Creates router
            .as("Router access rule")
            .because("Router should only be accessed by FilterChain, Pipeline, App")
            .check(importedClasses);
    }


    /**
     * Plugins must only be instantiated by App at startup and subsequently called by the router or
     * the execution chain (FilterChainImpl).
     */
    @Test
    void pluginRule () {
        ArchRuleDefinition.classes()
            .that().resideInAPackage("..plugin..")
            .should().onlyBeAccessed().byClassesThat(
                resideInAPackage("..router..")
                    .or(resideInAPackage("..plugin.."))
                    .or(simpleName("App")) //App creates plugin
                    .or(simpleName("FilterChainImpl"))) //FilterChainImpl calls the plugin after the router has decided which one to run.
            .as("Plugin access rule")
            .because("Plugins should only be managed by the Router, App, FilterChainImpl")
            .check(importedClasses);
    }

    /**
     * The HttpResponseWriter is the final step in the request lifecycle, responsible for delivering the response to the client.
     * This rule ensures that only the ConnectionHandler accesses it, guaranteeing controlled delivery and architectural integrity.
     */
    @Test
    void httpResponseWriterAccessRule() {
        ArchRuleDefinition.classes()
            .that().haveSimpleName("HttpResponseWriter")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("ConnectionHandler")
                    .or(simpleName("HttpResponseWriter")))
            .as("HttpResponseWriter access rule")
            .because("HttpResponseWriter is the final step in the lifecycle and should" +
                "only be used by ConnectionHandler to ensure controlled delivery")
            .check(importedClasses);
    }
}


