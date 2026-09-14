package pe.edu.upeu.saludablemente.aptitudfisica.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.DetallePruebaFisicaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.CatalogoPruebaRepository;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.DetallePruebaFisicaRepository;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.EvaluacionAptitudRepository;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EvaluacionAptitudTransactionalIntegrationTest {

    @Autowired
    private EvaluacionAptitudService evaluacionAptitudService;

    @Autowired
    private EvaluacionAptitudRepository evaluacionAptitudRepository;

    @Autowired
    private DetallePruebaFisicaRepository detallePruebaFisicaRepository;

    @Autowired
    private CatalogoPruebaRepository catalogoPruebaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private PersonaService personaService;

    @AfterEach
    void cleanDatabase() {
        evaluacionAptitudRepository.deleteAll();
        catalogoPruebaRepository.deleteAll();
    }

    @Test
    void registrarEvaluacionCommitsHeaderDetailsAndCalculatedValuesTogether() {
        CatalogoPrueba resistencia = guardarPrueba("Resistencia");
        CatalogoPrueba velocidad = guardarPrueba("Velocidad");

        EvaluacionAptitudResponseDto response = evaluacionAptitudService.registrarEvaluacion(
                request(resistencia.getId(), new BigDecimal("18.00"), velocidad.getId(), new BigDecimal("16.00")));

        assertThat(response.getIdEvaluacionAptitud()).isNotNull();
        assertThat(response.getDetalles()).hasSize(2);
        assertThat(response.getPuntajeGlobal()).isEqualByComparingTo("34.00");
        assertThat(response.getDiagnosticoAptitud()).isEqualTo("Bueno");
        assertThat(response.getSincronizado()).isTrue();
        assertThat(evaluacionAptitudRepository.count()).isEqualTo(1);
        assertThat(detallePruebaFisicaRepository.count()).isEqualTo(2);

        var persisted = evaluacionAptitudRepository.findById(response.getIdEvaluacionAptitud()).orElseThrow();
        assertThat(persisted.getDetalles()).hasSize(2);
        assertThat(persisted.getPuntajeGlobal()).isEqualByComparingTo("34.00");
        assertThat(persisted.getDiagnosticoAptitud()).isEqualTo("Bueno");
    }

    @Test
    void failureAfterRegistrationParticipatesInTransactionAndRollsBackHeaderAndDetails() {
        CatalogoPrueba resistencia = guardarPrueba("Resistencia");
        CatalogoPrueba velocidad = guardarPrueba("Velocidad");
        EvaluacionAptitudRequestDto request = request(
                resistencia.getId(), new BigDecimal("18.00"), velocidad.getId(), new BigDecimal("16.00"));

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            evaluacionAptitudService.registrarEvaluacion(request);

            assertThat(evaluacionAptitudRepository.count()).isEqualTo(1);
            assertThat(detallePruebaFisicaRepository.count()).isEqualTo(2);
            throw new IllegalStateException("Simulated failure after compound registration");
        })).isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated failure after compound registration");

        assertThat(evaluacionAptitudRepository.count()).isZero();
        assertThat(detallePruebaFisicaRepository.count()).isZero();
    }

    private CatalogoPrueba guardarPrueba(String nombre) {
        CatalogoPrueba prueba = new CatalogoPrueba();
        prueba.setNombrePrueba(nombre);
        prueba.setUnidadMedida("puntos");
        prueba.setActivo(true);
        return catalogoPruebaRepository.save(prueba);
    }

    private EvaluacionAptitudRequestDto request(Long primeraPruebaId, BigDecimal primerPuntaje,
                                                 Long segundaPruebaId, BigDecimal segundoPuntaje) {
        DetallePruebaFisicaDto primerDetalle = DetallePruebaFisicaDto.builder()
                .idPrueba(primeraPruebaId)
                .valorObtenido(new BigDecimal("10.00"))
                .puntajeParcial(primerPuntaje)
                .build();
        DetallePruebaFisicaDto segundoDetalle = DetallePruebaFisicaDto.builder()
                .idPrueba(segundaPruebaId)
                .valorObtenido(new BigDecimal("12.00"))
                .puntajeParcial(segundoPuntaje)
                .build();

        EvaluacionAptitudRequestDto request = new EvaluacionAptitudRequestDto();
        request.setIdPersona(100L);
        request.setFechaRegistro(LocalDate.now());
        request.setDetalles(List.of(primerDetalle, segundoDetalle));
        return request;
    }
}
