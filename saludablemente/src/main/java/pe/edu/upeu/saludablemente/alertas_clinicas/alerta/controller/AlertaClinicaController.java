package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.AlertaService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas Clínicas", description = "Endpoints para creación manual, consulta paginada y métricas agregadas de alertas clínicas.")
public class AlertaClinicaController {

    private final AlertaService alertaService;

    @PostMapping
    @Operation(summary = "Crear alerta clínica", description = "Evalúa síncronamente los indicadores contra el catálogo OMRON, calcula scoring y persiste cabecera-detalle.")
    public ResponseEntity<AlertaDetalleResponseDTO> crearAlerta(@Valid @RequestBody AlertaRequestDTO request) {
        AlertaDetalleResponseDTO response = alertaService.crearAlerta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/inbox")
    @Operation(summary = "Bandeja de entrada (Inbox)", description = "Obtiene alertas filtradas por severidad, estado, indicador y paginación.")
    public ResponseEntity<Page<AlertaInboxResponseDTO>> obtenerInbox(@ModelAttribute FiltroInboxRequestDTO filtro) {
        Page<AlertaInboxResponseDTO> page = alertaService.obtenerBandejaInbox(filtro);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle clínico", description = "Devuelve el análisis clínico profundo y los detalles de los indicadores para una alerta específica.")
    public ResponseEntity<AlertaDetalleResponseDTO> obtenerPorId(@PathVariable("id") UUID id) {
        AlertaDetalleResponseDTO response = alertaService.obtenerAlertaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de métricas", description = "Devuelve estadísticas agregadas (totales por estado, críticas, promedio de score) con ceros seguros.")
    public ResponseEntity<AlertaAgregadoDTO> obtenerResumen() {
        AlertaAgregadoDTO resumen = alertaService.obtenerResumenMetricas();
        return ResponseEntity.ok(resumen);
    }
}
