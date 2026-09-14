package pe.edu.upeu.saludablemente.exportacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.auditoria.service.AuditoriaService;
import pe.edu.upeu.saludablemente.exportacion.entity.LogInteroperabilidadFhirEntity;
import pe.edu.upeu.saludablemente.exportacion.repository.LogInteroperabilidadFhirRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/fhir/r4")
@RequiredArgsConstructor
@Tag(name = "FHIR Observation", description = "Interoperabilidad FHIR R4 - Observaciones")
public class FhirObservationRestController {

    private final LogInteroperabilidadFhirRepository logRepository;
    private final AuditoriaService auditoriaService;

    @GetMapping(value = "/Observation", produces = "application/fhir+json")
    @Operation(summary = "Buscar observaciones clinicas")
    public ResponseEntity<Map<String, Object>> buscarObservation(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String code,
            HttpServletRequest request) {

        long inicio = System.currentTimeMillis();

        Map<String, Object> bundle = new HashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("id", UUID.randomUUID().toString());
        bundle.put("type", "searchset");
        bundle.put("timestamp", LocalDateTime.now().toString());
        bundle.put("total", 0);
        bundle.put("entry", List.of());

        int tiempoMs = (int) (System.currentTimeMillis() - inicio);
        registrarLog("Observation", "patient=" + patient + "&code=" + code, 200, request, tiempoMs);

        auditoriaService.registrarLectura(null, "FHIR_OBSERVATION", patient != null ? patient : "ALL", "Busqueda FHIR Observation");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(bundle);
    }

    @GetMapping(value = "/Observation/{id}", produces = "application/fhir+json")
    @Operation(summary = "Obtener Observation por ID")
    public ResponseEntity<Map<String, Object>> obtenerObservation(
            @PathVariable String id,
            HttpServletRequest request) {

        long inicio = System.currentTimeMillis();

        Map<String, Object> obs = new HashMap<>();
        obs.put("resourceType", "Observation");
        obs.put("id", id);
        obs.put("status", "final");

        int tiempoMs = (int) (System.currentTimeMillis() - inicio);
        registrarLog("Observation", "id=" + id, 200, request, tiempoMs);

        auditoriaService.registrarLectura(null, "FHIR_OBSERVATION", id, "Consulta FHIR Observation");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(obs);
    }

    private void registrarLog(String recurso, String parametros, int codigoHttp,
                              HttpServletRequest request, int tiempoMs) {
        try {
            logRepository.save(LogInteroperabilidadFhirEntity.builder()
                    .idSistemaCliente(request.getHeader("X-Client-Id") != null ? request.getHeader("X-Client-Id") : "SISTEMA_EXTERNO")
                    .recursoSolicitado(recurso)
                    .parametrosConsulta(parametros)
                    .codigoHttpRespuesta(codigoHttp)
                    .direccionIp(obtenerIp(request))
                    .tiempoRespuestaMs(tiempoMs)
                    .fechaPeticion(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Error al registrar log FHIR: {}", e.getMessage());
        }
    }

    private String obtenerIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
