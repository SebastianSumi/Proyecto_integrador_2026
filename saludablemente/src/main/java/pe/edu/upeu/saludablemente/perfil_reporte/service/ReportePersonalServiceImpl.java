package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.EvaluacionNoDisponibleException;
import pe.edu.upeu.saludablemente.exception.ReporteNoEncontradoException;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.GenerarReporteRequestDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfGenerationResultDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfMetadataDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteGeneradoResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteHistorialResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReporteDescargaBitacoraEntity;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReportePersonalEntity;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.EstadoGeneracionReporte;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.TipoAlmacenamiento;
import pe.edu.upeu.saludablemente.perfil_reporte.mapper.ReportePersonalMapper;
import pe.edu.upeu.saludablemente.perfil_reporte.repository.ReporteDescargaBitacoraRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.repository.ReportePersonalRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.storage.StorageAdapter;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportePersonalServiceImpl implements ReportePersonalService {

    private final ReportePersonalRepository reporteRepository;
    private final ReporteDescargaBitacoraRepository bitacoraRepository;
    private final PerfilDashboardService perfilDashboardService;
    private final SnapshotJsonBuilderService snapshotBuilder;
    private final PdfGeneratorService pdfGeneratorService;
    private final StorageAdapter storageAdapter;
    private final ReportePersonalMapper mapper;

    @Override
    @Transactional
    public ReporteGeneradoResponseDTO generarReporte(GenerarReporteRequestDTO request, Long idUsuarioSolicitante) {
        log.info("Generando reporte para persona {} periodo {}", request.getIdPersona(), request.getPeriodoSemestral());

        var existente = reporteRepository.findFirstByIdPersonaAndPeriodoSemestralOrderByFechaGeneracionDesc(
                request.getIdPersona(), request.getPeriodoSemestral());

        if (existente.isPresent() && !Boolean.TRUE.equals(request.getForzarRegeneracion())) {
            log.info("Reporte existente encontrado, devolviendo referencia sin regenerar");
            return mapper.toGeneradoResponseDTO(existente.get());
        }

        PerfilDashboardResponseDTO dashboard = perfilDashboardService.consolidarDashboard(request.getIdPersona());

        if (dashboard == null || dashboard.getEvaluacionActual() == null) {
            throw new EvaluacionNoDisponibleException(
                    "El colaborador no tiene evaluacion registrada para el periodo " + request.getPeriodoSemestral());
        }

        if (Boolean.TRUE.equals(request.getForzarRegeneracion())) {
            reporteRepository.desactivarReportesVigentes(request.getIdPersona());
        }

        Integer versionActual = reporteRepository.findMaxVersionByPersonaAndPeriodo(
                request.getIdPersona(), request.getPeriodoSemestral());
        int nuevaVersion = (versionActual == null ? 0 : versionActual) + 1;

        UUID idReporte = UUID.randomUUID();

        String snapshotJson = snapshotBuilder.serializarSnapshotInmutable(
                dashboard, request.getPeriodoSemestral(), idReporte.toString());

        SnapshotDatosDTO snapshotDTO = snapshotBuilder.deserializarSnapshot(snapshotJson);
        PdfMetadataDTO pdfMetadata = PdfMetadataDTO.builder()
                .idPersona(request.getIdPersona())
                .periodoSemestral(request.getPeriodoSemestral())
                .idReporte(idReporte.toString())
                .versionPlantilla("1.0")
                .generadoPor("usuario_" + idUsuarioSolicitante)
                .build();

        PdfGenerationResultDTO pdfResult = pdfGeneratorService.generarPdf(snapshotDTO, pdfMetadata);

        String claveAlmacenamiento = request.getPeriodoSemestral()
                + "/" + request.getIdPersona() + "/" + idReporte + ".pdf";
        String rutaAlmacenamiento = storageAdapter.guardarArchivo(claveAlmacenamiento, pdfResult.getPdfBytes());

        ReportePersonalEntity reporte = ReportePersonalEntity.builder()
                .idReporte(idReporte)
                .idPersona(request.getIdPersona())
                .nombreColaborador(dashboard.getFiliacion() != null
                        ? dashboard.getFiliacion().getNombreCompleto() : null)
                .periodoSemestral(request.getPeriodoSemestral())
                .versionReporte(nuevaVersion)
                .snapshotDatos(snapshotJson)
                .rutaAlmacenamiento(rutaAlmacenamiento)
                .tipoAlmacenamiento(TipoAlmacenamiento.LOCAL)
                .tamanoBytes(pdfResult.getTamanoBytes())
                .hashIntegridadSha256(pdfResult.getHashSha256())
                .estadoGeneracion(EstadoGeneracionReporte.COMPLETADO)
                .generadoPor("usuario_" + idUsuarioSolicitante)
                .vigente(true)
                .build();

        ReportePersonalEntity guardado = reporteRepository.save(reporte);

        log.info("Reporte generado exitosamente: {} version {}", guardado.getIdReporte(), guardado.getVersionReporte());

        return mapper.toGeneradoResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteHistorialResponseDTO> obtenerHistorial(Long idPersona) {
        log.debug("Consultando historial de reportes para persona {}", idPersona);
        return reporteRepository.findByIdPersonaOrderByFechaGeneracionDesc(idPersona)
                .stream()
                .map(mapper::toHistorialDTO)
                .toList();
    }

    @Override
    @Transactional
    public void registrarDescarga(UUID idReporte, Long idPersonaSolicitante, String ip, String userAgent) {
        log.debug("Registrando descarga de reporte {}", idReporte);
        ReporteDescargaBitacoraEntity bitacora = ReporteDescargaBitacoraEntity.builder()
                .idReporte(idReporte)
                .idPersonaSolicitante(idPersonaSolicitante)
                .direccionIp(ip)
                .userAgent(userAgent)
                .build();
        bitacoraRepository.save(bitacora);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarIntegridad(UUID idReporte) {
        ReportePersonalEntity reporte = reporteRepository.findById(idReporte)
                .orElseThrow(() -> new ReporteNoEncontradoException("Reporte no encontrado: " + idReporte));

        try {
            byte[] pdfBytes = storageAdapter.obtenerArchivo(reporte.getRutaAlmacenamiento());
            String hashCalculado = calcularHash(pdfBytes);
            boolean integro = hashCalculado.equals(reporte.getHashIntegridadSha256());
            if (!integro) {
                log.error("Violacion de integridad detectada para reporte {}", idReporte);
            }
            return integro;
        } catch (Exception e) {
            log.error("Error al verificar integridad: {}", e.getMessage());
            return false;
        }
    }

    private String calcularHash(byte[] contenido) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }
}
