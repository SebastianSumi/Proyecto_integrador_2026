package pe.edu.upeu.saludablemente.alertas_clinicas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaDetalleRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaDetalleResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.mapper.AlertaMapper;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.AlertaServiceImpl;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.ConsolidacionDuplicadosService;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.MaquinariaEstadosService;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.SnapshotInmutableService;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.catalog.OmronReferenceCatalog;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.ScoreDetalleDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.SindromeResultadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.MotivoDesestimacionRequeridoException;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.TransicionEstadoInvalidaException;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.CatalogoAccionCorrectivaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.mapper.ResolucionMapper;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository.CatalogoAccionRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository.ResolucionClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service.DesestimacionService;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service.ProgramadorSeguimientoService;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service.ResolucionServiceImpl;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service.TrazabilidadResponsableService;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertasClinicasServiceTests {

    private OmronReferenceCatalog omronCatalog;
    private CalculadoraDesviacionService calculadoraDesviacion;
    private MatrizSeveridadService matrizSeveridadService;
    private AgrupacionSindromesService agrupacionSindromesService;
    private MaquinariaEstadosService maquinariaEstadosService;
    private DesestimacionService desestimacionService;
    private ProgramadorSeguimientoService programadorSeguimientoService;
    private TrazabilidadResponsableService trazabilidadResponsableService;

    @Mock
    private AlertaClinicaRepository alertaRepository;
    @Mock
    private AlertaMapper alertaMapper;
    @Mock
    private ConsolidacionDuplicadosService consolidacionDuplicadosService;
    @Mock
    private SnapshotInmutableService snapshotInmutableService;
    @Mock
    private MultiplicadorReincidenciaService multiplicadorReincidenciaService;

    @Mock
    private ResolucionClinicaRepository resolucionRepository;
    @Mock
    private CatalogoAccionRepository catalogoAccionRepository;
    @Mock
    private ResolucionMapper resolucionMapper;

    private AlertaServiceImpl alertaService;
    private ResolucionServiceImpl resolucionService;

    @BeforeEach
    void setUp() {
        omronCatalog = new OmronReferenceCatalog();
        calculadoraDesviacion = new CalculadoraDesviacionService();
        matrizSeveridadService = new MatrizSeveridadService();
        agrupacionSindromesService = new AgrupacionSindromesService(matrizSeveridadService);
        maquinariaEstadosService = new MaquinariaEstadosService();
        desestimacionService = new DesestimacionService();
        programadorSeguimientoService = new ProgramadorSeguimientoService();
        trazabilidadResponsableService = new TrazabilidadResponsableService();

        alertaService = new AlertaServiceImpl(
                alertaRepository,
                alertaMapper,
                omronCatalog,
                calculadoraDesviacion,
                matrizSeveridadService,
                agrupacionSindromesService,
                multiplicadorReincidenciaService,
                consolidacionDuplicadosService,
                snapshotInmutableService
        );

        resolucionService = new ResolucionServiceImpl(
                resolucionRepository,
                catalogoAccionRepository,
                alertaRepository,
                maquinariaEstadosService,
                trazabilidadResponsableService,
                desestimacionService,
                programadorSeguimientoService,
                resolucionMapper
        );
    }

    @Test
    @DisplayName("Cálculo de desvío porcentual matemático OMRON")
    void testCalculadoraDesviacion() {
        // Glucosa: medido 150, límite 100 -> desvío 50.00%
        BigDecimal desvio = calculadoraDesviacion.calcularDesviacion(new BigDecimal("150"), new BigDecimal("100"));
        assertEquals(new BigDecimal("50.00"), desvio);

        // Sin desvío: medido 90, límite 100 -> 0.00%
        BigDecimal sinDesvio = calculadoraDesviacion.calcularDesviacion(new BigDecimal("90"), new BigDecimal("100"));
        assertEquals(new BigDecimal("0.00"), sinDesvio);
    }

    @Test
    @DisplayName("Detección de Síndrome Metabólico (Glucosa + Triglicéridos + Presión)")
    void testDeteccionSindromeMetabolico() {
        List<IndicadorEvaluadoDTO> indicadores = List.of(
                IndicadorEvaluadoDTO.builder().tipoIndicador(TipoIndicador.GLUCOSA).esAlterado(true).build(),
                IndicadorEvaluadoDTO.builder().tipoIndicador(TipoIndicador.TRIGLICERIDOS).esAlterado(true).build(),
                IndicadorEvaluadoDTO.builder().tipoIndicador(TipoIndicador.PRESION_SISTOLICA).esAlterado(true).build()
        );

        SindromeResultadoDTO resultado = agrupacionSindromesService.detectarSindrome(indicadores);
        assertNotNull(resultado);
        assertEquals("RIESGO_SINDROME_METABOLICO", resultado.getCodigo());
        assertEquals(10, resultado.getPuntosExtra());
    }

    @Test
    @DisplayName("Determinación de Nivel de Severidad en Matriz")
    void testDeterminacionSeveridad() {
        assertEquals(NivelSeveridad.CRITICO, matrizSeveridadService.determinarSeveridad(15));
        assertEquals(NivelSeveridad.MODERADO, matrizSeveridadService.determinarSeveridad(8));
        assertEquals(NivelSeveridad.LEVE, matrizSeveridadService.determinarSeveridad(3));
    }

    @Test
    @DisplayName("Máquina de estados: validar transiciones legales y bloquear ilegales")
    void testMaquinariaEstados() {
        AlertaClinicaEntity alerta = AlertaClinicaEntity.builder()
                .idAlerta(UUID.randomUUID())
                .estado(EstadoAlerta.PENDIENTE)
                .build();

        // Legal: PENDIENTE -> EN_REVISION
        maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.EN_REVISION);
        assertEquals(EstadoAlerta.EN_REVISION, alerta.getEstado());

        // Legal: EN_REVISION -> ATENDIDA
        maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.ATENDIDA);
        assertEquals(EstadoAlerta.ATENDIDA, alerta.getEstado());

        // Ilegal: ATENDIDA (terminal) -> PENDIENTE
        assertThrows(TransicionEstadoInvalidaException.class, () ->
                maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.PENDIENTE));
    }

    @Test
    @DisplayName("Desestimación clínica: exigir motivo válido y estructurado")
    void testDesestimacionValidacion() {
        DesestimarAlertaRequestDTO reqVacio = DesestimarAlertaRequestDTO.builder()
                .idAlerta(UUID.randomUUID())
                .motivoDesestimacion("")
                .build();

        assertThrows(MotivoDesestimacionRequeridoException.class, () ->
                desestimacionService.validarMotivoDesestimacion(reqVacio));

        DesestimarAlertaRequestDTO reqInvalido = DesestimarAlertaRequestDTO.builder()
                .idAlerta(UUID.randomUUID())
                .motivoDesestimacion("NO_ME_GUSTA")
                .build();

        assertThrows(MotivoDesestimacionRequeridoException.class, () ->
                desestimacionService.validarMotivoDesestimacion(reqInvalido));

        DesestimarAlertaRequestDTO reqValido = DesestimarAlertaRequestDTO.builder()
                .idAlerta(UUID.randomUUID())
                .motivoDesestimacion("BAJO_TRATAMIENTO_PREVIO")
                .build();

        assertDoesNotThrow(() -> desestimacionService.validarMotivoDesestimacion(reqValido));
    }

    @Test
    @DisplayName("Crear alerta clínica síncrona transaccional")
    void testCrearAlertaSincrona() {
        UUID alertaId = UUID.randomUUID();
        AlertaRequestDTO request = AlertaRequestDTO.builder()
                .idPersona(101L)
                .nombrePaciente("Carlos Mendoza")
                .detalles(List.of(
                        AlertaDetalleRequestDTO.builder()
                                .tipoIndicador(TipoIndicador.GLUCOSA)
                                .valorMedido(new BigDecimal("140"))
                                .build()
                ))
                .build();

        when(consolidacionDuplicadosService.encontrarDuplicado(any(), any(), any()))
                .thenReturn(Optional.empty());

        AlertaClinicaEntity entidadGuardada = AlertaClinicaEntity.builder()
                .idAlerta(alertaId)
                .idPersona(101L)
                .nombrePaciente("Carlos Mendoza")
                .nivelSeveridad(NivelSeveridad.LEVE)
                .scoreRiesgo(3)
                .estado(EstadoAlerta.PENDIENTE)
                .fechaGeneracion(LocalDateTime.now())
                .fechaVencimientoSla(LocalDateTime.now().plusHours(72))
                .build();

        when(alertaRepository.save(any(AlertaClinicaEntity.class))).thenReturn(entidadGuardada);
        when(alertaMapper.toDetalleDTO(any(AlertaClinicaEntity.class))).thenReturn(
                AlertaDetalleResponseDTO.builder()
                        .idAlerta(alertaId)
                        .idPersona(101L)
                        .nombrePaciente("Carlos Mendoza")
                        .nivelSeveridad(NivelSeveridad.LEVE)
                        .scoreRiesgo(3)
                        .estado(EstadoAlerta.PENDIENTE)
                        .horasSlaRestantes(72L)
                        .build()
        );

        AlertaDetalleResponseDTO response = alertaService.crearAlerta(request);

        assertNotNull(response);
        assertEquals(alertaId, response.getIdAlerta());
        assertEquals("Carlos Mendoza", response.getNombrePaciente());
        verify(alertaRepository, times(1)).save(any(AlertaClinicaEntity.class));
    }

    @Test
    @DisplayName("Atender alerta clínica y programar seguimiento")
    void testAtenderAlerta() {
        UUID alertaId = UUID.randomUUID();
        AlertaClinicaEntity alerta = AlertaClinicaEntity.builder()
                .idAlerta(alertaId)
                .estado(EstadoAlerta.PENDIENTE)
                .build();

        CatalogoAccionCorrectivaEntity accion = CatalogoAccionCorrectivaEntity.builder()
                .idAccion(1L)
                .codigoTipificado("AJUSTE_DIETA")
                .descripcion("Ajuste nutricional hipocalórico")
                .requiereSeguimiento("S")
                .diasSeguimiento(30)
                .build();

        when(alertaRepository.findById(alertaId)).thenReturn(Optional.of(alerta));
        when(resolucionRepository.existsByAlerta_IdAlerta(alertaId)).thenReturn(false);
        when(catalogoAccionRepository.findById(1L)).thenReturn(Optional.of(accion));

        AtenderAlertaRequestDTO request = AtenderAlertaRequestDTO.builder()
                .idAlerta(alertaId)
                .idAccionCorrectiva(1L)
                .observacionesClinicas("Paciente orientado en reducción de carbohidratos.")
                .idUsuarioEvaluador(5L)
                .nombreEvaluador("Dra. Valdivia")
                .build();

        resolucionService.atenderAlerta(request);

        assertEquals(EstadoAlerta.ATENDIDA, alerta.getEstado());
        verify(resolucionRepository, times(1)).save(any(ResolucionClinicaEntity.class));
        verify(alertaRepository, times(1)).save(alerta);
    }
}
