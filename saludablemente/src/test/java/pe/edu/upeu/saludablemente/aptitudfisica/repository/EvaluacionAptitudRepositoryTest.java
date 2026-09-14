package pe.edu.upeu.saludablemente.aptitudfisica.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.DetallePruebaFisica;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.EvaluacionAptitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluacionAptitudRepositoryTest {

    @Autowired
    private EvaluacionAptitudRepository evaluacionAptitudRepository;

    @Autowired
    private CatalogoPruebaRepository catalogoPruebaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private CatalogoPrueba catalogo() {
        CatalogoPrueba prueba = new CatalogoPrueba();
        prueba.setNombrePrueba("Carrera 100m");
        prueba.setUnidadMedida("seg");
        prueba.setActivo(true);
        return prueba;
    }

    private DetallePruebaFisica detalle(CatalogoPrueba prueba, EvaluacionAptitud evaluacion, BigDecimal puntaje) {
        DetallePruebaFisica detalle = new DetallePruebaFisica();
        detalle.setValorObtenido(new BigDecimal("12.50"));
        detalle.setPuntajeParcial(puntaje);
        detalle.setCatalogoPrueba(prueba);
        detalle.setEvaluacionAptitud(evaluacion);
        return detalle;
    }

    private EvaluacionAptitud evaluacion(Long idPersona, LocalDate fecha, String diagnostico, BigDecimal puntaje,
                                         boolean sincronizado, CatalogoPrueba prueba, int detalles) {
        EvaluacionAptitud evaluacion = new EvaluacionAptitud();
        evaluacion.setIdPersona(idPersona);
        evaluacion.setFechaRegistro(fecha);
        evaluacion.setPuntajeGlobal(puntaje);
        evaluacion.setDiagnosticoAptitud(diagnostico);
        evaluacion.setSincronizado(sincronizado);
        for (int i = 0; i < detalles; i++) {
            evaluacion.getDetalles().add(detalle(prueba, evaluacion, BigDecimal.valueOf(10 + i)));
        }
        return evaluacion;
    }

    @Test
    void findByIdCargaDetallesConEntityGraph() {
        CatalogoPrueba prueba = catalogoPruebaRepository.save(catalogo());
        EvaluacionAptitud evaluacion = evaluacion(1L, LocalDate.of(2026, 1, 10), "Bueno",
                new BigDecimal("30.00"), true, prueba, 2);
        testEntityManager.persistAndFlush(evaluacion);
        testEntityManager.clear();

        Optional<EvaluacionAptitud> resultado = evaluacionAptitudRepository.findById(evaluacion.getId());

        assertTrue(resultado.isPresent());
        assertEquals(2, resultado.get().getDetalles().size());
    }

    @Test
    void buscarResumenProyectaCantidadDeDetalles() {
        CatalogoPrueba prueba = catalogoPruebaRepository.save(catalogo());
        EvaluacionAptitud v1 = evaluacion(1L, LocalDate.of(2026, 1, 10), "Bueno",
                new BigDecimal("30.00"), true, prueba, 1);
        EvaluacionAptitud v2 = evaluacion(2L, LocalDate.of(2026, 2, 5), "Regular",
                new BigDecimal("20.00"), false, prueba, 0);
        testEntityManager.persistAndFlush(v1);
        testEntityManager.persistAndFlush(v2);
        testEntityManager.clear();

        List<EvaluacionAptitudResumenDto> resumen = evaluacionAptitudRepository
                .buscarResumen(null, null, null, null, Sort.by("id"));

        assertEquals(2, resumen.size());
        assertEquals("Bueno", resumen.get(0).getDiagnosticoAptitud());
        assertEquals(1, resumen.get(0).getCantidadDetalles());

        List<EvaluacionAptitudResumenDto> soloPersona = evaluacionAptitudRepository
                .buscarResumen(2L, null, null, null, Sort.by("id"));
        assertEquals(1, soloPersona.size());
        assertEquals(0, soloPersona.get(0).getCantidadDetalles());
    }

    @Test
    void agregadosSumaPuntajesConCoalesce() {
        CatalogoPrueba prueba = catalogoPruebaRepository.save(catalogo());
        EvaluacionAptitud v1 = evaluacion(1L, LocalDate.of(2026, 1, 10), "Bueno",
                new BigDecimal("30.00"), true, prueba, 1);
        EvaluacionAptitud v2 = evaluacion(1L, LocalDate.of(2026, 2, 5), "Regular",
                new BigDecimal("20.00"), false, prueba, 0);
        testEntityManager.persistAndFlush(v1);
        testEntityManager.persistAndFlush(v2);
        testEntityManager.clear();

        EvaluacionAptitudAgregadoDto agregado = evaluacionAptitudRepository.agregados(null);

        assertEquals(2, agregado.getTotalEvaluaciones());
        assertEquals(0, new BigDecimal("50.00").compareTo(agregado.getSumaPuntajeGlobal()));

        EvaluacionAptitudAgregadoDto sinDatos = evaluacionAptitudRepository.agregados(999L);
        assertEquals(0, sinDatos.getTotalEvaluaciones());
        assertNotNull(sinDatos.getSumaPuntajeGlobal());
        assertEquals(0, BigDecimal.ZERO.compareTo(sinDatos.getSumaPuntajeGlobal()));
    }
}