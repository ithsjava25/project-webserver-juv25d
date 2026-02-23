package org.juv25d;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;


import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;

/**
 * These tests ensure that classes only depend on other classes according to the intended architecture.
 * They enforce strict boundaries between components and prevent unintended coupling or dependency violations.
 *
 * The request lifecycle is designed to follow this strict flow:
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
 * Each component has a clearly defined responsibility and must not violate the intended direction of dependencies.
 */

@AnalyzeClasses(
packages = "org.juv25d",
importOptions = ImportOption.Predefined.DoNotIncludeTests.class)

public class ArchitectureTest {


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

    @ArchTest
    static final ArchRule pipelineAccessRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Pipeline")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("ConnectionHandler")
                    .or(simpleName("ConnectionHandlerFactory"))
                    .or(simpleName("Pipeline"))
                    .or(simpleName("Server"))) //TODO right now server creates pipeline. Shold this be handled by connectionHandler instead to keep the strict flow?
                    .as("Pipeline access rule")
                    .because("Pipeline should only be accessed by ConnectionHandler");

    @ArchTest
    static final ArchRule filterChainRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("FilterChain")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("Pipeline")
                    .or(simpleName("FilterChain"))
                    .or(simpleName("FilterChainImpl"))
                    .or(simpleName("ConnectionHandler"))) // TODO This needs to be accessed because connectionhandler creates doFilter()
                    .as("FilterChain access rule")
                    .because("FilterChain should only be accessed by Pipeline");

    @ArchTest
    static final ArchRule routerRule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("Router")
            .should().onlyBeAccessed().byClassesThat(
                simpleName("FilterChain")
                    .or(simpleName("FilterChainImpl"))
                    .or(simpleName("Router"))
                    .or(simpleName("Pipeline")) //TODO Pipeline injects router
                    .or(simpleName("App"))) //TODO App Creates router
                    .as("Router access rule")
                    .because("Router should only be accessed by FilterChain");

}


