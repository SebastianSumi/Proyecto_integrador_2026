package pe.edu.upeu.saludablemente.actividades.inscripcion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.SolicitudLoteInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.SolicitudInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.RespuestaInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.service.InscripcionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/actividades/{activityId}/inscripciones")
@Tag(name = "Inscripciones", description = "Inscripciones de personas a actividades")
public class InscripcionController {
    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    @Operation(summary = "Listar inscripciones de una actividad")
    public List<RespuestaInscripcion> findAll(
            @PathVariable Long activityId,
            @RequestParam(name = "incluirCanceladas", defaultValue = "false") boolean includeCancelled) {
        return inscripcionService.findByActivity(activityId, includeCancelled);
    }

    @PostMapping
    @Operation(summary = "Inscribir una persona")
    public ResponseEntity<RespuestaInscripcion> enroll(@PathVariable Long activityId,
                                                      @Valid @RequestBody SolicitudInscripcion request) {
        return ResponseEntity.status(201).body(inscripcionService.enroll(activityId, request.personId()));
    }

    @PostMapping("/lote")
    @Operation(summary = "Inscribir un lote atómico de personas")
    public ResponseEntity<List<RespuestaInscripcion>> enrollBatch(
            @PathVariable Long activityId,
            @Valid @RequestBody SolicitudLoteInscripcion request) {
        return ResponseEntity.status(201).body(inscripcionService.enrollBatch(activityId, request.personIds()));
    }

    @DeleteMapping("/{personId}")
    @Operation(summary = "Cancelar lógicamente una inscripción")
    public RespuestaInscripcion cancel(@PathVariable Long activityId, @PathVariable Long personId) {
        return inscripcionService.cancel(activityId, personId);
    }
}
