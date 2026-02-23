package org.juv25d;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;


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

@AnalyzeClasses(
packages = "org.juv25d",
importOptions = ImportOption.Predefined.DoNotIncludeTests.class)

public class ArchitectureTest {


    /**
     *  This rule ensures that only the Server and its associated factories can initiate a ConnectionHandler.
     *  This prevents other parts of the application from accidentally manipulating direct client connections.
     */
    @ArchTest
    static final ArchRule connectionHandlerAccessRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("ConnectionHandler")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("Server")
                    .or(simpleName("ConnectionHandler"))
                    .or(simpleName("DefaultConnectionHandlerFactory"))
                    .or(simpleName("ConnectionHandlerFactory")))
            .as("ConnectionHandler access rule")
            .because("connectionHandler should only be accessed by server");


    /**
     * Only the network layer (ConnectionHandler) or the application's startup class (App) may interact with the Pipeline.
     * This guarantees that the execution chain remains intact and is not modified during an active request.
     */
    @ArchTest
    static final ArchRule pipelineAccessRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Pipeline")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("ConnectionHandler")
                    .or(simpleName("ConnectionHandlerFactory"))
                    .or(simpleName("Pipeline"))
                    .or(simpleName("App")) // App handles bootstrapping and wiring of the Pipeline during startup. This should stay.
                    .or(simpleName("Server"))) //TODO right now server creates pipeline. Shold this be handled by connectionHandler instead to keep the strict flow?
            .as("Pipeline access rule")
            .because("Pipeline should only be accessed by ConnectionHandler");


    /**
     * The FilterChain is created by the Pipeline and triggered by the ConnectionHandler.
     * This rule also allows individual filters to access the chain.
     */
    @ArchTest
    static final ArchRule filterChainRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("FilterChain")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("Pipeline")
                    .or(simpleName("FilterChain"))
                    .or(simpleName("FilterChainImpl"))
                    .or(resideInAPackage("..filter.."))
                    .or(simpleName("ConnectionHandler"))) //This needs to be accessed because connectionhandler creates doFilter()
            .as("FilterChain access rule")
            .because("FilterChain should only be accessed by Pipeline");


    /**
     * The Router should only be accessed by the FilterChain to determine which plugin to execute,
     * or by App and Pipeline during the system's bootstrapping phase.
     */
    @ArchTest
    static final ArchRule routerRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Router")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("FilterChain")
                    .or(simpleName("FilterChainImpl"))
                    .or(simpleName("Router"))
                    .or(simpleName("Pipeline")) //Pipeline injects router
                    .or(simpleName("App"))) //App Creates router
            .as("Router access rule")
            .because("Router should only be accessed by FilterChain");


    /**
     * Plugins must only be instantiated by App at startup and subsequently called by the router or
     * the execution chain (FilterChainImpl).
     */
    @ArchTest
    static final ArchRule pluginRule =
        ArchRuleDefinition.classes()
            .that().resideInAPackage("..plugin..")
            .should().onlyBeAccessed().byClassesThat(
                resideInAPackage("..router..")
                    .or(resideInAPackage("..plugin.."))
                    .or(simpleName("App")) //App creates plugin
                    .or(simpleName("FilterChainImpl"))) //FilterChainImpl calls the plugin after the router has decided which one to run.
            .as("Plugin access rule")
            .because("Plugins should only be managed by the Router or during startup");
}


