package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.GestionSlaEnrutamientoService;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.AlertaDetalleResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.AlertaInboxResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.FiltroInboxRequestDTO;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/alertas/inbox")
@RequiredArgsConstructor
@Tag(name = "Bandeja de Entrada", description = "API para gestión de bandeja de entrada de alertas")
public class BandejaEntradaInboxController {

    private final GestionSlaEnrutamientoService gestionSlaEnrutamientoService;

    @PostMapping("/buscar")
    @Operation(summary = "Buscar alertas en la bandeja de entrada")
    public ResponseEntity<Page<AlertaInboxResponseDTO>> buscarAlertas(
            @RequestBody FiltroInboxRequestDTO filtro) {

        log.debug(" Buscando alertas con filtros: {}", filtro);
        Page<AlertaInboxResponseDTO> resultados = gestionSlaEnrutamientoService
                .obtenerBandejaInbox(filtro);

        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/{idAlerta}")
    @Operation(summary = "Obtener detalle de una alerta")
    public ResponseEntity<AlertaDetalleResponseDTO> obtenerDetalle(
            @PathVariable UUID idAlerta) {

        log.debug("📄 Obteniendo detalle de alerta: {}", idAlerta);
        AlertaDetalleResponseDTO detalle = gestionSlaEnrutamientoService
                .obtenerDetalleAlerta(idAlerta);

        return ResponseEntity.ok(detalle);
    }

    @PostMapping("/{idAlerta}/enrutar")
    @Operation(summary = "Enrutar y asignar una alerta")
    public ResponseEntity<Map<String, Object>> enrutarAlerta(
            @PathVariable UUID idAlerta) {

        log.info(" Enrutando alerta: {}", idAlerta);
        var evaluadorId = gestionSlaEnrutamientoService.enrutarYAsignarAlerta(idAlerta);

        return ResponseEntity.ok(Map.of(
                "idAlerta", idAlerta,
                "evaluadorAsignado", evaluadorId.orElse(null),
                "mensaje", evaluadorId.isPresent() ?
                        "Alerta enrutada exitosamente" :
                        "No hay evaluadores disponibles"
        ));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de alertas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestParam(required = false) Long idEvaluador) {

        log.debug(" Obteniendo estadísticas para evaluador: {}", idEvaluador);
        Map<String, Object> estadisticas = gestionSlaEnrutamientoService
                .obtenerEstadisticas(idEvaluador != null ? idEvaluador : 0L);

        return ResponseEntity.ok(estadisticas);
    }
}