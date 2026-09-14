package pe.edu.upeu.saludablemente.exportacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.auditoria.service.AuditoriaService;
import pe.edu.upeu.saludablemente.exportacion.entity.LogInteroperabilidadFhirEntity;
import pe.edu.upeu.saludablemente.exportacion.repository.LogInteroperabilidadFhirRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/fhir/r4")
@RequiredArgsConstructor
@Tag(name = "FHIR Bundle", description = "Interoperabilidad FHIR R4 - Bundles completos")
public class FhirBundleRestController {

    private final LogInteroperabilidadFhirRepository logRepository;
    private final AuditoriaService auditoriaService;

    @GetMapping(value = "/Bundle/{id}", produces = "application/fhir+json")
    @Operation(summary = "Obtener Bundle completo por ID")
    public ResponseEntity<Map<String, Object>> obtenerBundle(
            @PathVariable String id,
            HttpServletRequest request) {

        long inicio = System.currentTimeMillis();

        Map<String, Object> bundle = Map.of(
                "resourceType", "Bundle",
                "id", id != null ? id : UUID.randomUUID().toString(),
                "type", "document",
                "timestamp", LocalDateTime.now().toString(),
                "total", 0,
                "entry", List.of()
        );

        int tiempoMs = (int) (System.currentTimeMillis() - inicio);
        registrarLog("Bundle", "id=" + id, 200, request, tiempoMs);

        auditoriaService.registrarLectura(null, "FHIR_BUNDLE", id, "Consulta FHIR Bundle");

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
