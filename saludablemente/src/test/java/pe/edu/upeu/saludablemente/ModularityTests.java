package pe.edu.upeu.saludablemente;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(SaludablementeApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new org.springframework.modulith.docs.Documenter(modules)
                .writeDocumentation();
    }
}
