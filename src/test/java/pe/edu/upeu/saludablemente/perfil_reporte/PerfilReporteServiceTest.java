package pe.edu.upeu.saludablemente.perfil_reporte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.perfil_reporte.client.*;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.*;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReportePersonalEntity;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.EstadoGeneracionReporte;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.TipoAlmacenamiento;
import pe.edu.upeu.saludablemente.perfil_reporte.mapper.ReportePersonalMapper;
import pe.edu.upeu.saludablemente.perfil_reporte.mapper.ReportePersonalMapperImpl;
import pe.edu.upeu.saludablemente.perfil_reporte.repository.ReporteDescargaBitacoraRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.repository.ReportePersonalRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.service.*;
import pe.edu.upeu.saludablemente.perfil_reporte.storage.StorageAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilReporteServiceTest {

    private AnaliticaLongitudinalService analiticaService;
    private PdfDigitalSignerService signerService;
    private ChartRendererService chartService;
    private QrCodeGeneratorService qrService;
    private PerfilDashboardService dashboardService;
    private ReportePersonalService reporteService;
    private ReportePersonalMapper mapper;

    @Mock
    private ReportePersonalRepository reporteRepository;
    @Mock
    private ReporteDescargaBitacoraRepository bitacoraRepository;
    @Mock
    private StorageAdapter storageAdapter;
    @Mock
    private SnapshotJsonBuilderService snapshotBuilder;
    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @BeforeEach
    void setUp() {
        analiticaService = new AnaliticaLongitudinalServiceImpl();
        signerService = new PdfDigitalSignerServiceImpl();
        chartService = new ChartRendererServiceImpl();
        qrService = new QrCodeGeneratorServiceImpl();
        mapper = new ReportePersonalMapperImpl();

        PersonalClient personalClient = new PersonalClient();
        EvaluacionClient evaluacionClient = new EvaluacionClient();
        AptitudFisicaClient aptitudFisicaClient = new AptitudFisicaClient();
        AlertasClient alertasClient = new AlertasClient();
        RecomendacionClient recomendacionClient = new RecomendacionClient();
        MetasClient metasClient = new MetasClient();
        ActividadesClient actividadesClient = new ActividadesClient();

        dashboardService = new PerfilDashboardServiceImpl(
                personalClient,
                evaluacionClient,
                aptitudFisicaClient,
                alertasClient,
                recomendacionClient,
                metasClient,
                actividadesClient,
                analiticaService
        );

        reporteService = new ReportePersonalServiceImpl(
                reporteRepository,
                bitacoraRepository,
                dashboardService,
                snapshotBuilder,
                pdfGeneratorService,
                storageAdapter,
                mapper
        );
    }

    @Test
    void testAnaliticaLongitudinalDeltas() {
        EvaluacionClinicaResumenDTO actual = EvaluacionClinicaResumenDTO.builder()
                .imc(new BigDecimal("28.00"))
                .nivelGrasaVisceral(10)
                .glucosaMgDl(new BigDecimal("105.00"))
                .build();

        List<EvaluacionHistoricoItemDTO> historial = List.of(
                EvaluacionHistoricoItemDTO.builder()
                        .periodo("2026-1")
                        .imc(new BigDecimal("29.50"))
                        .nivelGrasaVisceral(12)
                        .glucosaMgDl(new BigDecimal("110.00"))
                        .build()
        );

        AnaliticaLongitudinalDTO resultado = analiticaService.procesarEvolucion(actual, historial);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("-1.50"), resultado.getDeltaImcSemestreAnterior());
        assertEquals(new BigDecimal("-2.00"), resultado.getDeltaGrasaVisceralSemestreAnterior());
        assertEquals(new BigDecimal("-5.00"), resultado.getDeltaGlucosaSemestreAnterior());
        assertEquals("MEJORA_SIGNIFICATIVA", resultado.getTendenciaMetabolica());
    }

    @Test
    void testCalcularSemaforoGlobal() {
        EvaluacionClinicaResumenDTO evaluacionOptima = EvaluacionClinicaResumenDTO.builder()
                .imc(new BigDecimal("22.0"))
                .presionSistolica(115)
                .presionDiastolica(75)
                .glucosaMgDl(new BigDecimal("90.0"))
                .build();

        String semaforoOptimo = analiticaService.calcularSemaforoGlobal(evaluacionOptima, List.of());
        assertEquals("OPTIMO", semaforoOptimo);

        List<AlertaPacienteResponseDTO> alertasCriticas = List.of(
                AlertaPacienteResponseDTO.builder()
                        .tipoIndicador("Presion")
                        .estadoAtencion("Alerta critica")
                        .build()
        );

        String semaforoCritico = analiticaService.calcularSemaforoGlobal(evaluacionOptima, alertasCriticas);
        assertEquals("ATENCION_REQUERIDA", semaforoCritico);
    }

    @Test
    void testPdfDigitalSigner() {
        String data = "Reporte de Prueba Salud Ocupacional";
        String hash = signerService.calcularHashSha256(data);

        assertNotNull(hash);
        assertEquals(64, hash.length());

        String firma = signerService.generarFirmaDigital(hash, "UUID-12345");
        assertNotNull(firma);
        assertEquals(64, firma.length());
    }

    @Test
    void testChartRendererSvg() {
        String radar = chartService.generarRadarChartSvg("Test", List.of("A", "B", "C"), List.of(50.0, 75.0, 100.0));
        assertNotNull(radar);
        assertTrue(radar.contains("<svg"));
        assertTrue(radar.contains("polygon"));

        String bar = chartService.generarBarChartSvg("Test Bar", List.of("Grasa", "Musculo"), List.of(25.0, 35.0));
        assertNotNull(bar);
        assertTrue(bar.contains("<svg"));
        assertTrue(bar.contains("<rect"));
    }

    @Test
    void testQrGenerator() {
        String qr = qrService.generarQrBase64("https://saludablemente.upeu.edu.pe", 100, 100);
        assertNotNull(qr);
        assertFalse(qr.isBlank());
    }

    @Test
    void testConsolidarDashboard() {
        PerfilDashboardResponseDTO dashboard = dashboardService.consolidarDashboard(1L);

        assertNotNull(dashboard);
        assertNotNull(dashboard.getFiliacion());
        assertEquals(1L, dashboard.getFiliacion().getIdPersona());
        assertNotNull(dashboard.getEvaluacionActual());
        assertNotNull(dashboard.getAnaliticaHistorica());
        assertNotNull(dashboard.getAptitudFisica());
        assertNotNull(dashboard.getAlertasActivas());
        assertNotNull(dashboard.getRecomendacionIA());
        assertNotNull(dashboard.getMetas());
        assertNotNull(dashboard.getAgendaActividades());
    }

    @Test
    void testGenerarReporteConIdempotencia() {
        GenerarReporteRequestDTO request = GenerarReporteRequestDTO.builder()
                .idPersona(1L)
                .periodoSemestral("2026-2")
                .forzarRegeneracion(false)
                .build();

        UUID idExistente = UUID.randomUUID();
        ReportePersonalEntity existente = ReportePersonalEntity.builder()
                .idReporte(idExistente)
                .idPersona(1L)
                .periodoSemestral("2026-2")
                .versionReporte(1)
                .tamanoBytes(1024L)
                .hashIntegridadSha256("hash123")
                .fechaGeneracion(LocalDateTime.now())
                .rutaAlmacenamiento("2026-2/1/reporte.pdf")
                .estadoGeneracion(EstadoGeneracionReporte.COMPLETADO)
                .tipoAlmacenamiento(TipoAlmacenamiento.LOCAL)
                .generadoPor("usuario_1")
                .vigente(true)
                .build();

        when(reporteRepository.findFirstByIdPersonaAndPeriodoSemestralOrderByFechaGeneracionDesc(1L, "2026-2"))
                .thenReturn(Optional.of(existente));

        ReporteGeneradoResponseDTO response = reporteService.generarReporte(request, 1L);

        assertNotNull(response);
        assertEquals(idExistente, response.getIdReporte());
        assertEquals("/api/v1/perfil/reportes/" + idExistente + "/descargar", response.getUrlDescarga());
    }

    @Test
    void testVerificarIntegridad() {
        UUID idReporte = UUID.randomUUID();
        byte[] fakePdf = "Contenido PDF de prueba".getBytes();
        String hashReal = signerService.calcularHashSha256(fakePdf);

        ReportePersonalEntity entity = ReportePersonalEntity.builder()
                .idReporte(idReporte)
                .rutaAlmacenamiento("ruta/pdf.pdf")
                .hashIntegridadSha256(hashReal)
                .build();

        when(reporteRepository.findById(idReporte)).thenReturn(Optional.of(entity));
        when(storageAdapter.obtenerArchivo("ruta/pdf.pdf")).thenReturn(fakePdf);

        boolean integro = reporteService.verificarIntegridad(idReporte);
        assertTrue(integro);
    }
}
