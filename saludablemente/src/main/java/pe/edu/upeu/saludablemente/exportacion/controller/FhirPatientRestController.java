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
@Tag(name = "FHIR Patient", description = "Interoperabilidad FHIR R4 - Pacientes")
public class FhirPatientRestController {

    private final LogInteroperabilidadFhirRepository logRepository;
    private final AuditoriaService auditoriaService;

    @GetMapping(value = "/Patient/{id}", produces = "application/fhir+json")
    @Operation(summary = "Obtener recurso Patient por ID")
    public ResponseEntity<Map<String, Object>> obtenerPatient(
            @PathVariable String id,
            HttpServletRequest request) {

        long inicio = System.currentTimeMillis();
        Map<String, Object> patient = new HashMap<>();
        patient.put("resourceType", "Patient");
        patient.put("id", id);
        patient.put("active", true);

        int tiempoMs = (int) (System.currentTimeMillis() - inicio);
        registrarLog("Patient", "id=" + id, 200, request, tiempoMs);

        // Auditoría síncrona
        Long idPersona = null;
        try { idPersona = Long.parseLong(id); } catch (Exception ignored) {}
        auditoriaService.registrarLectura(idPersona, "FHIR_PATIENT", id, "Consulta FHIR Patient");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(patient);
    }

    @GetMapping(value = "/Patient", produces = "application/fhir+json")
    @Operation(summary = "Buscar Patient por identificador")
    public ResponseEntity<Map<String, Object>> buscarPatient(
            @RequestParam(required = false) String identifier,
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
        registrarLog("Patient", "identifier=" + identifier, 200, request, tiempoMs);

        auditoriaService.registrarLectura(null, "FHIR_PATIENT", identifier != null ? identifier : "ALL", "Busqueda FHIR Patient");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(bundle);
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
