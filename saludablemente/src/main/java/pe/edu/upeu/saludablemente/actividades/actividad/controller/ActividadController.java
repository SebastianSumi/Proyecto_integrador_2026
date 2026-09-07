package pe.edu.upeu.saludablemente.actividades.actividad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.RespuestaActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudEstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/actividades")
@Tag(name = "Actividades", description = "Gestión de actividades del programa de salud")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    @Operation(summary = "Listar actividades", description = "Filtra opcionalmente por estado.")
    public List<RespuestaActividad> findAll(@RequestParam(required = false, name = "estado") String state) {
        return actividadService.findAll(state == null ? null : EstadoActividad.fromPublicValue(state));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una actividad por ID")
    public RespuestaActividad findById(@PathVariable Long id) {
        return actividadService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad registrada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Horario solapado")
    })
    public ResponseEntity<RespuestaActividad> create(@Valid @RequestBody SolicitudActividad request) {
        RespuestaActividad response = actividadService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una actividad programada")
    public RespuestaActividad update(@PathVariable Long id, @Valid @RequestBody SolicitudActividad request) {
        return actividadService.update(id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de una actividad",
            description = "Define el estado deseado de forma explícita e idempotente.")
    public RespuestaActividad updateState(@PathVariable Long id,
                                        @Valid @RequestBody SolicitudEstadoActividad request) {
        return actividadService.updateState(id, request);
    }
}
