package pe.edu.upeu.saludablemente;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.springframework.modulith.core.ApplicationModules.*;

class ModularityTests {

    ApplicationModules modules = of(SaludablementeApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }
}
