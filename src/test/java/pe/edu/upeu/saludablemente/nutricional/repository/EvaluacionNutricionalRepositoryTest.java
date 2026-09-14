package pe.edu.upeu.saludablemente.nutricional.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalAgregadoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResumenDto;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleAntropometrico;
import pe.edu.upeu.saludablemente.nutricional.entity.EstadoEvaluacionNutricional;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;

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
class EvaluacionNutricionalRepositoryTest {

    @Autowired
    private EvaluacionNutricionalRepository evaluacionNutricionalRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private DetalleAntropometrico detalle(EvaluacionNutricional evaluacion, BigDecimal imc, BigDecimal grasa) {
        DetalleAntropometrico detalle = new DetalleAntropometrico();
        detalle.setEstaturaCm(new BigDecimal("170.00"));
        detalle.setPesoKg(new BigDecimal("70.00"));
        detalle.setImc(imc);
        detalle.setPorcentajeGrasa(grasa);
        detalle.setEvaluacionNutricional(evaluacion);
        return detalle;
    }

    private EvaluacionNutricional evaluacion(Long idPersona, EstadoEvaluacionNutricional estado,
                                             String periodo, LocalDate fecha) {
        EvaluacionNutricional evaluacion = new EvaluacionNutricional();
        evaluacion.setIdPersona(idPersona);
        evaluacion.setEstadoEvaluacion(estado);
        evaluacion.setPeriodoSemestral(periodo);
        evaluacion.setFechaEvaluacion(fecha);
        return evaluacion;
    }

    @Test
    void findByIdCargaDetallesConEntityGraph() {
        EvaluacionNutricional evaluacion = evaluacion(1L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-I", LocalDate.of(2026, 1, 15));
        evaluacion.setDetalleAntropometrico(detalle(evaluacion, new BigDecimal("24.22"), new BigDecimal("20.0")));
        testEntityManager.persistAndFlush(evaluacion);
        testEntityManager.clear();

        Optional<EvaluacionNutricional> resultado = evaluacionNutricionalRepository.findById(evaluacion.getId());

        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getDetalleAntropometrico());
        assertEquals(0, new BigDecimal("24.22").compareTo(resultado.get().getDetalleAntropometrico().getImc()));
    }

    @Test
    void buscarFiltraPorEstadoYPeriodo() {
        EvaluacionNutricional completa = evaluacion(1L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-I", LocalDate.of(2026, 1, 15));
        completa.setDetalleAntropometrico(detalle(completa, new BigDecimal("24.22"), new BigDecimal("20.0")));
        EvaluacionNutricional enProceso = evaluacion(1L, EstadoEvaluacionNutricional.EN_PROCESO,
                "2026-I", LocalDate.of(2026, 2, 1));
        EvaluacionNutricional otroPeriodo = evaluacion(2L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-II", LocalDate.of(2026, 3, 1));
        testEntityManager.persistAndFlush(completa);
        testEntityManager.persistAndFlush(enProceso);
        testEntityManager.persistAndFlush(otroPeriodo);
        testEntityManager.clear();

        List<EvaluacionNutricional> porEstado = evaluacionNutricionalRepository
                .buscar(1L, EstadoEvaluacionNutricional.COMPLETA, null, null, null, Sort.by("id"));
        assertEquals(1, porEstado.size());

        List<EvaluacionNutricional> porPeriodo = evaluacionNutricionalRepository
                .buscar(null, null, "2026-II", null, null, Sort.by("id"));
        assertEquals(1, porPeriodo.size());
        assertEquals(2L, porPeriodo.get(0).getIdPersona());
    }

    @Test
    void buscarResumenProyectaDatos() {
        EvaluacionNutricional completa = evaluacion(1L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-I", LocalDate.of(2026, 1, 15));
        testEntityManager.persistAndFlush(completa);
        testEntityManager.clear();

        List<EvaluacionNutricionalResumenDto> resumen = evaluacionNutricionalRepository
                .buscarResumen(1L, EstadoEvaluacionNutricional.COMPLETA, null, null, null, Sort.by("id"));

        assertEquals(1, resumen.size());
        assertEquals(EstadoEvaluacionNutricional.COMPLETA, resumen.get(0).getEstadoEvaluacion());
        assertEquals("2026-I", resumen.get(0).getPeriodoSemestral());
    }

    @Test
    void agregadosCalculaPromediosIgnorandoSinAntropometria() {
        EvaluacionNutricional e1 = evaluacion(1L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-I", LocalDate.of(2026, 1, 15));
        e1.setDetalleAntropometrico(detalle(e1, new BigDecimal("24.22"), new BigDecimal("20.0")));
        EvaluacionNutricional e2 = evaluacion(1L, EstadoEvaluacionNutricional.COMPLETA,
                "2026-I", LocalDate.of(2026, 1, 20));
        e2.setDetalleAntropometrico(detalle(e2, new BigDecimal("28.50"), new BigDecimal("28.0")));
        EvaluacionNutricional e3 = evaluacion(3L, EstadoEvaluacionNutricional.EN_PROCESO,
                "2026-I", LocalDate.of(2026, 2, 1));
        testEntityManager.persistAndFlush(e1);
        testEntityManager.persistAndFlush(e2);
        testEntityManager.persistAndFlush(e3);
        testEntityManager.clear();

        EvaluacionNutricionalAgregadoDto agregado = evaluacionNutricionalRepository.agregados(null, null);

        assertEquals(3, agregado.getTotalEvaluaciones());
        assertEquals((24.22 + 28.50) / 2, agregado.getImcPromedio(), 0.01);
        assertEquals((20.0 + 28.0) / 2, agregado.getGrasaPromedio(), 0.01);
    }
}