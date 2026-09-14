package pe.edu.upeu.saludablemente.exportacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fhir/r4")
@RequiredArgsConstructor
@Tag(name = "FHIR Metadata", description = "CapabilityStatement del servidor FHIR")
public class FhirCapabilityStatementController {

    @GetMapping(value = "/metadata", produces = "application/fhir+json")
    @Operation(summary = "Obtener CapabilityStatement de capacidades FHIR")
    public ResponseEntity<Map<String, Object>> obtenerCapabilityStatement() {

        Map<String, Object> capability = new HashMap<>();
        capability.put("resourceType", "CapabilityStatement");
        capability.put("id", "saludablemente-fhir-r4");
        capability.put("url", "http://saludablemente.upeu.edu.pe/fhir/r4/metadata");
        capability.put("version", "1.0.0");
        capability.put("name", "SaludablementeFHIRServer");
        capability.put("title", "Servidor FHIR Saludablemente UPeU");
        capability.put("status", "active");
        capability.put("experimental", false);
        capability.put("date", LocalDateTime.now().toLocalDate().toString());
        capability.put("publisher", "Universidad Peruana Union");
        capability.put("kind", "instance");
        capability.put("software", Map.of(
                "name", "Saludablemente Exportacion",
                "version", "1.0.0"
        ));
        capability.put("implementation", Map.of(
                "description", "Servidor FHIR R4 para interoperabilidad de salud ocupacional",
                "url", "http://saludablemente.upeu.edu.pe/fhir/r4"
        ));
        capability.put("fhirVersion", "4.0.1");
        capability.put("format", List.of("application/fhir+json"));
        capability.put("rest", List.of(Map.of(
                "mode", "server",
                "resource", List.of(
                        Map.of("type", "Patient", "interaction", List.of(
                                Map.of("code", "read"),
                                Map.of("code", "search-type"))),
                        Map.of("type", "Observation", "interaction", List.of(
                                Map.of("code", "read"),
                                Map.of("code", "search-type"))),
                        Map.of("type", "Bundle", "interaction", List.of(
                                Map.of("code", "read")))
                )
        )));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(capability);
    }
}
