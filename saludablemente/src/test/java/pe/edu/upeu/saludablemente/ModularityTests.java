package pe.edu.upeu.saludablemente;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifiesModuleBoundaries() {
        ApplicationModules.of(SaludablementeApplication.class).verify();
    }
}