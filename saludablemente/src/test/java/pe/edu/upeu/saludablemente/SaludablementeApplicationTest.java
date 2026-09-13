package pe.edu.upeu.saludablemente;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class SaludablementeApplicationTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(SaludablementeApplication.class).verify();
    }
}