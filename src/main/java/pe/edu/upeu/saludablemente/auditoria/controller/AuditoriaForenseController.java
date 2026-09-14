package pe.edu.upeu.saludablemente.auditoria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditoriaForenseResponseDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.ExportacionForenseRequestDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.FiltroAuditoriaForenseRequestDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.IntegridadLedgerResponseDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.PaqueteEvidenciaDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.TimelineEventoDTO;
import pe.edu.upeu.saludablemente.auditoria.service.BuscadorSemanticoJsonService;
import pe.edu.upeu.saludablemente.auditoria.service.ExportadorEvidenciaForenseService;
import pe.edu.upeu.saludablemente.auditoria.service.TimelineReconstructionService;
import pe.edu.upeu.saludablemente.auditoria.service.VerificacionIntegridadService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auditoria/forense")
@RequiredArgsConstructor
@Tag(name = "Auditoria Forense", description = "API para consultas forenses de auditoria")
@PreAuthorize("hasRole('ROLE_OFICIAL_SEGURIDAD') or hasRole('ROLE_AUDITOR_MEDICO') or hasRole('ROLE_ADMIN')")
public class AuditoriaForenseController {

    private final BuscadorSemanticoJsonService buscadorService;
    private final TimelineReconstructionService timelineService;
    private final VerificacionIntegridadService integridadService;
    private final ExportadorEvidenciaForenseService exportadorService;

    @PostMapping("/buscar")
    @Operation(summary = "Buscar eventos de auditoria con filtros avanzados")
    public ResponseEntity<AuditoriaForenseResponseDTO> buscar(
            @Valid @RequestBody FiltroAuditoriaForenseRequestDTO filtro) {

        log.info("Busqueda forense solicitada con filtros");
        AuditoriaForenseResponseDTO resultado = buscadorService.buscar(filtro);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/timeline/{entidadAfectada}/{idEntidad}")
    @Operation(summary = "Reconstruir timeline de una entidad")
    public ResponseEntity<List<TimelineEventoDTO>> obtenerTimeline(
            @PathVariable String entidadAfectada,
            @PathVariable String idEntidad) {

        log.info("Timeline solicitado para {}:{}", entidadAfectada, idEntidad);
        List<TimelineEventoDTO> timeline = timelineService.reconstruirTimeline(
                entidadAfectada, idEntidad);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/reconstruir/{entidadAfectada}/{idEntidad}")
    @Operation(summary = "Reconstruir estado de una entidad en una fecha especifica")
    public ResponseEntity<Map<String, Object>> reconstruirEstado(
            @PathVariable String entidadAfectada,
            @PathVariable String idEntidad,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {

        log.info("Reconstruccion solicitada para {}:{} en fecha {}",
                entidadAfectada, idEntidad, fechaCorte);
        Map<String, Object> estado = timelineService.reconstruirEstadoEnFecha(
                entidadAfectada, idEntidad, fechaCorte);
        return ResponseEntity.ok(estado);
    }

    @GetMapping("/verificar/cadena-completa")
    @Operation(summary = "Verificar integridad de toda la cadena de auditoria")
    public ResponseEntity<IntegridadLedgerResponseDTO> verificarCadenaCompleta() {
        log.info("Verificacion de cadena completa solicitada");
        return ResponseEntity.ok(integridadService.verificarCadenaCompleta());
    }

    @GetMapping("/verificar/desde/{secuenciaInicio}")
    @Operation(summary = "Verificar integridad desde una secuencia especifica")
    public ResponseEntity<IntegridadLedgerResponseDTO> verificarDesde(
            @PathVariable Long secuenciaInicio) {
        log.info("Verificacion desde secuencia {} solicitada", secuenciaInicio);
        return ResponseEntity.ok(integridadService.verificarDesde(secuenciaInicio));
    }

    @GetMapping("/verificar/registro/{idBitacora}")
    @Operation(summary = "Verificar integridad de un registro individual")
    public ResponseEntity<IntegridadLedgerResponseDTO> verificarRegistro(
            @PathVariable UUID idBitacora) {
        log.info("Verificacion de registro {} solicitada", idBitacora);
        return ResponseEntity.ok(integridadService.verificarRegistroIndividual(idBitacora));
    }

    @PostMapping("/exportar")
    @Operation(summary = "Exportar evidencias forenses firmadas digitalmente")
    public ResponseEntity<byte[]> exportarEvidencia(
            @Valid @RequestBody ExportacionForenseRequestDTO request) {

        log.info("Exportacion forense solicitada en formato {}", request.getFormato());
        PaqueteEvidenciaDTO paquete = exportadorService.exportarEvidencia(request);

        String contentType = "JSON".equalsIgnoreCase(paquete.getFormato())
                ? MediaType.APPLICATION_JSON_VALUE
                : "text/csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + paquete.getNombreArchivo() + "\"")
                .header("X-Evidencia-Hash-SHA256", paquete.getHashSha256())
                .header("X-Evidencia-Firma-RSA", paquete.getFirmaRsa() != null ? paquete.getFirmaRsa() : "")
                .header("X-Evidencia-Total-Registros", String.valueOf(paquete.getTotalRegistrosIncluidos()))
                .contentType(MediaType.parseMediaType(contentType))
                .body(paquete.getContenido());
    }
}
