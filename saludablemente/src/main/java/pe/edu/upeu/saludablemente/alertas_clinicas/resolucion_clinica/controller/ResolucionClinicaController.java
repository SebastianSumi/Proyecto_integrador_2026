package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.ResolucionClinicaOrquestadorService;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/alertas/resolucion")
@RequiredArgsConstructor
@Tag(name = "Resolución Clínica", description = "API para resolución de alertas clínicas")
public class ResolucionClinicaController {

    private final ResolucionClinicaOrquestadorService resolucionService;

    // Simulación de usuario autenticado (en producción vendría del SecurityContext)
    private static final Long USUARIO_EVALUADOR = 1L;
    private static final String NOMBRE_EVALUADOR = "Dr. Juan Perez";

    @PostMapping("/atender")
    @Operation(summary = "Atender una alerta clínica")
    public ResponseEntity<Map<String, Object>> atenderAlerta(
            @Valid @RequestBody AtenderAlertaRequestDTO request) {

        log.info("📥 Solicitud de atención para alerta: {}", request.getIdAlerta());

        ResolucionClinicaEntity resolucion = resolucionService.atenderAlerta(
                request, USUARIO_EVALUADOR, NOMBRE_EVALUADOR
        );

        Map<String, Object> response = new HashMap<>();
        response.put("idResolucion", resolucion.getIdResolucion());
        response.put("idAlerta", resolucion.getAlerta().getIdAlerta());
        response.put("estado", resolucion.getAlerta().getEstado());
        response.put("fechaAtencion", resolucion.getFechaAtencion());
        response.put("mensaje", "Alerta atendida exitosamente");
        response.put("seguimientoProgramado", resolucion.getEsSeguimientoProgramado());
        response.put("fechaSeguimiento", resolucion.getFechaSeguimientoProgramado());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/desestimar")
    @Operation(summary = "Desestimar una alerta clínica")
    public ResponseEntity<Map<String, Object>> desestimarAlerta(
            @Valid @RequestBody DesestimarAlertaRequestDTO request) {

        log.info("📥 Solicitud de desestimación para alerta: {}", request.getIdAlerta());

        ResolucionClinicaEntity resolucion = resolucionService.desestimarAlerta(
                request, USUARIO_EVALUADOR, NOMBRE_EVALUADOR
        );

        Map<String, Object> response = new HashMap<>();
        response.put("idResolucion", resolucion.getIdResolucion());
        response.put("idAlerta", resolucion.getAlerta().getIdAlerta());
        response.put("estado", resolucion.getAlerta().getEstado());
        response.put("fechaAtencion", resolucion.getFechaAtencion());
        response.put("motivoDesestimacion", resolucion.getMotivoDesestimacion());
        response.put("mensaje", "Alerta desestimada exitosamente");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/catalogo/acciones")
    @Operation(summary = "Obtener catálogo de acciones correctivas")
    public ResponseEntity<List<CatalogoAccionResponseDTO>> obtenerCatalogoAcciones() {
        List<CatalogoAccionResponseDTO> catalogo = resolucionService.obtenerCatalogoAcciones();
        return ResponseEntity.ok(catalogo);
    }

    @GetMapping("/catalogo/motivos-desestimacion")
    @Operation(summary = "Obtener motivos válidos para desestimación")
    public ResponseEntity<List<String>> obtenerMotivosDesestimacion() {
        List<String> motivos = resolucionService.obtenerMotivosDesestimacion();
        return ResponseEntity.ok(motivos);
    }

    @GetMapping("/resolucion/{idAlerta}")
    @Operation(summary = "Obtener resolución por ID de alerta")
    public ResponseEntity<ResolucionClinicaEntity> obtenerResolucion(
            @PathVariable UUID idAlerta) {
        ResolucionClinicaEntity resolucion = resolucionService.obtenerResolucionPorAlerta(idAlerta);
        return ResponseEntity.ok(resolucion);
    }
}