package org.juv25d;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;


import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;


@AnalyzeClasses(packages = "org.juv25d")
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
            .because("connectionHandler should only be accessed by server")
            ;
}

