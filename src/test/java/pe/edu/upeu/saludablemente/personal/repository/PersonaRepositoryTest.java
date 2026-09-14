package pe.edu.upeu.saludablemente.personal.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.entity.PreferenciaComunicacion;
import pe.edu.upeu.saludablemente.personal.entity.Sexo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonaRepositoryTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Persona personaActiva() {
        Persona persona = new Persona();
        persona.setNombres("Juan");
        persona.setApellidoPaterno("Perez");
        persona.setApellidoMaterno("Lopez");
        persona.setCelular("900111222");
        persona.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        persona.setSexo(Sexo.M);
        persona.setActivo(true);

        PreferenciaComunicacion preferencia = new PreferenciaComunicacion();
        preferencia.setCanalPreferido("WhatsApp");
        preferencia.setAceptaRecordatorios(true);
        preferencia.setPersona(persona);
        persona.setPreferenciaComunicacion(preferencia);
        return persona;
    }

    @Test
    void findByIdCargaPreferenciaConEntityGraph() {
        Persona guardada = testEntityManager.persistAndFlush(personaActiva());
        testEntityManager.clear();

        Optional<Persona> resultado = personaRepository.findById(guardada.getId());

        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getPreferenciaComunicacion());
        assertEquals("WhatsApp", resultado.get().getPreferenciaComunicacion().getCanalPreferido());
    }

    @Test
    void buscarFiltraPorActivo() {
        Persona activa = personaActiva();
        Persona inactiva = personaActiva();
        inactiva.setNombres("Maria");
        inactiva.setCelular("900333444");
        inactiva.setActivo(false);
        testEntityManager.persistAndFlush(activa);
        testEntityManager.persistAndFlush(inactiva);
        testEntityManager.clear();

        List<Persona> resultado = personaRepository.buscar(true, null, null, Sort.by("id"));

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombres());
    }

    @Test
    void buscarFiltraPorNombresYCelular() {
        Persona juan = personaActiva();
        Persona maria = personaActiva();
        maria.setNombres("Maria");
        maria.setCelular("900333444");
        testEntityManager.persistAndFlush(juan);
        testEntityManager.persistAndFlush(maria);
        testEntityManager.clear();

        List<Persona> porNombre = personaRepository.buscar(null, "jua", null, Sort.by("id"));
        List<Persona> porCelular = personaRepository.buscar(null, null, "900333444", Sort.by("id"));

        assertEquals(1, porNombre.size());
        assertEquals(1, porCelular.size());
        assertEquals("Maria", porCelular.get(0).getNombres());
    }
}