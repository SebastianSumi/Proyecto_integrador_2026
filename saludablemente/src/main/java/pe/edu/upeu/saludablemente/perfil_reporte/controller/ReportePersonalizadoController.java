package pe.edu.upeu.saludablemente.perfil_reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.GenerarReporteRequestDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteGeneradoResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteHistorialResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.service.ReportePersonalService;
import pe.edu.upeu.saludablemente.perfil_reporte.service.StreamingPdfService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Reporte Personalizado", description = "Generacion y descarga de reportes PDF")
@RestController
@RequestMapping("/api/v1/perfil/reportes")
@RequiredArgsConstructor
public class ReportePersonalizadoController {

    private final ReportePersonalService reportePersonalService;
    private final StreamingPdfService streamingPdfService;

    private static final Long USUARIO_SIMULADO = 1L;

    @Operation(summary = "Genera un nuevo reporte personalizado")
    @PostMapping("/generar")
    public ResponseEntity<ReporteGeneradoResponseDTO> generarReporte(
            @Valid @RequestBody GenerarReporteRequestDTO request) {

        log.info("Solicitud de generacion de reporte para persona {}", request.getIdPersona());
        ReporteGeneradoResponseDTO response = reportePersonalService
                .generarReporte(request, USUARIO_SIMULADO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Descarga el reporte en PDF")
    @GetMapping("/{idReporte}/descargar")
    public ResponseEntity<StreamingResponseBody> descargarReporte(
            @PathVariable UUID idReporte,
            HttpServletRequest httpRequest) {

        log.info("Solicitud de descarga de reporte {}", idReporte);

        String nombreArchivo = streamingPdfService.obtenerNombreArchivo(idReporte);
        long tamano = streamingPdfService.obtenerTamanoArchivo(idReporte);
        StreamingResponseBody stream = streamingPdfService.obtenerStreamingDescarga(idReporte);

        reportePersonalService.registrarDescarga(
                idReporte,
                USUARIO_SIMULADO,
                obtenerIpCliente(httpRequest),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(tamano))
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream);
    }

    @Operation(summary = "Obtiene el historial de reportes de un colaborador")
    @GetMapping("/historial/{idPersona}")
    public ResponseEntity<List<ReporteHistorialResponseDTO>> obtenerHistorial(
            @PathVariable Long idPersona) {

        log.info("Consultando historial de reportes para persona {}", idPersona);
        return ResponseEntity.ok(reportePersonalService.obtenerHistorial(idPersona));
    }

    @Operation(summary = "Verifica la integridad de un reporte")
    @GetMapping("/{idReporte}/verificar-integridad")
    public ResponseEntity<Boolean> verificarIntegridad(@PathVariable UUID idReporte) {
        log.info("Verificando integridad del reporte {}", idReporte);
        return ResponseEntity.ok(reportePersonalService.verificarIntegridad(idReporte));
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
