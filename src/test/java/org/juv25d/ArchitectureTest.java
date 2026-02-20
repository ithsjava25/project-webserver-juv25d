package org.juv25d;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "org.juv25d")
public class ArchitectureTest {


    @ArchTest
    static final ArchRule rule =
        ArchRuleDefinition.classes()
            .that().haveSimpleName("ConnectionHandler")
            .should().onlyBeAccessed().byClassesThat()
            .haveSimpleName("Server")
            .orShould().haveSimpleName("ConnectionHandler")
            .orShould().haveSimpleName("DefaultConnectionHandlerFactory")
            .orShould().haveSimpleName("ConnectionHandlerFactory");
}

//    @ArchTest
//    public static final ArchRule lifecycleArchitecture = layeredArchitecture()
//        .consideringAllDependencies()
//
//        .layer("Server").definedBy("org.juv25d.Server..")
//        .layer("ConnectionHandler").definedBy("org.juv25d.ConnectionHandler..")
//
//        .whereLayer("ConnectionHandler").mayOnlyBeAccessedByLayers("Server");
//
//
//
//}
