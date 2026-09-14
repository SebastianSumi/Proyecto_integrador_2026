package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto.*;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.service.RecomendacionTransaccionalService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recomendaciones/evaluador")
@RequiredArgsConstructor
@Tag(name = "Recomendaciones IA - Evaluador", description = "API para evaluadores")
public class RecomendacionEvaluadorController {

    private final RecomendacionTransaccionalService recomendacionService;

    @GetMapping("/historial")
    @Operation(summary = "Obtener historial de recomendaciones de un colaborador")
    public ResponseEntity<List<RecomendacionHistorialItemDTO>> obtenerHistorial(
            @RequestParam Long idPersona) {

        log.info(" Consultando historial para persona: {}", idPersona);
        List<RecomendacionHistorialItemDTO> historial = recomendacionService
                .obtenerHistorialRecomendaciones(idPersona);

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/auditoria/{idRecomendacion}")
    @Operation(summary = "Obtener detalle de auditoría de una recomendación")
    public ResponseEntity<RecomendacionAuditoriaDetalleDTO> obtenerDetalleAuditoria(
            @PathVariable String idRecomendacion) {

        log.info("Consultando auditoría: {}", idRecomendacion);
        RecomendacionAuditoriaDetalleDTO detalle = recomendacionService
                .obtenerDetalleAuditoria(java.util.UUID.fromString(idRecomendacion));

        return ResponseEntity.ok(detalle);
    }

    @PostMapping("/regenerar")
    @Operation(summary = "Forzar regeneración de recomendación")
    public ResponseEntity<RecomendacionVigenteResponseDTO> regenerarRecomendacion(
            @Valid @RequestBody RegenerarRecomendacionRequestDTO request) {

        log.info(" Regenerando recomendación para persona: {}", request.getIdPersona());

        return ResponseEntity.ok(null);
    }
}