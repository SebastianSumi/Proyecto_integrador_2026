package pe.edu.upeu.saludablemente.perfil_reporte.snapshot_reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pe.edu.upeu.saludablemente.perfil_reporte.snapshot_reporte.dto.GenerarReporteRequestDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.snapshot_reporte.dto.ReporteGeneradoResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.snapshot_reporte.dto.ReporteHistorialResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.snapshot_reporte.service.ReportePersonalService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/perfil/reportes")
@RequiredArgsConstructor
@Tag(name = "Reporte Personalizado", description = "API para generacion y descarga de reportes")
public class ReportePersonalizadoController {

    private final ReportePersonalService reportePersonalService;

    @PostMapping("/generar")
    @Operation(summary = "Generar un nuevo reporte personalizado")
    public ResponseEntity<ReporteGeneradoResponseDTO> generarReporte(
            @Valid @RequestBody GenerarReporteRequestDTO request) {

        log.info("Solicitud de generacion de reporte para persona {}", request.getIdPersona());

        ReporteGeneradoResponseDTO response = reportePersonalService.generarReporte(
                request, 1L, "Sistema");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idReporte}/descargar")
    @Operation(summary = "Descargar reporte en PDF")
    public ResponseEntity<StreamingResponseBody> descargarReporte(
            @PathVariable UUID idReporte,
            HttpServletRequest httpRequest) {

        log.info("Solicitud de descarga de reporte {}", idReporte);

        StreamingResponseBody stream = outputStream -> {
            reportePersonalService.escribirPdfDirecto(idReporte, 1L, outputStream);
        };

        // Registrar descarga en bitacora
        reportePersonalService.registrarDescarga(
                idReporte,
                1L,
                obtenerIpCliente(httpRequest),
                httpRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_salud_" + idReporte + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream);
    }

    @GetMapping("/historial/{idPersona}")
    @Operation(summary = "Obtener historial de reportes de un colaborador")
    public ResponseEntity<List<ReporteHistorialResponseDTO>> obtenerHistorial(
            @PathVariable Long idPersona) {

        log.info("Consultando historial de reportes para persona {}", idPersona);

        List<ReporteHistorialResponseDTO> historial = reportePersonalService.obtenerHistorial(idPersona);

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{idReporte}/verificar-integridad")
    @Operation(summary = "Verificar integridad de un reporte")
    public ResponseEntity<Boolean> verificarIntegridad(@PathVariable UUID idReporte) {
        log.info("Verificando integridad de reporte {}", idReporte);
        boolean integro = reportePersonalService.verificarIntegridad(idReporte);
        return ResponseEntity.ok(integro);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}