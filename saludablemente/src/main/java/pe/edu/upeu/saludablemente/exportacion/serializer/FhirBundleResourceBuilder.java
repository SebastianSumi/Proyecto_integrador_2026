package pe.edu.upeu.saludablemente.exportacion.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ErrorSerializacionDatasetException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FhirBundleResourceBuilder implements SerializadorFormato {

    private final FhirResourceMapper resourceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        long inicio = System.currentTimeMillis();
        int totalColaboradores = 0;
        int totalRecursos = 0;

        try {
            Map<String, Object> bundle = new HashMap<>();
            bundle.put("resourceType", "Bundle");
            bundle.put("id", UUID.randomUUID().toString());
            bundle.put("type", "collection");
            bundle.put("timestamp", LocalDateTime.now().toString());

            List<Map<String, Object>> entries = new ArrayList<>();

            var iterator = datos.iterator();
            while (iterator.hasNext()) {
                CohorteClinicaDTO cohorte = iterator.next();

                Map<String, Object> patient = resourceMapper.construirPatient(cohorte);
                if (!patient.isEmpty()) {
                    entries.add(Map.of("resource", patient));
                    totalRecursos++;
                }

                List<Map<String, Object>> observations = resourceMapper.construirObservations(cohorte);
                for (Map<String, Object> obs : observations) {
                    entries.add(Map.of("resource", obs));
                    totalRecursos++;
                }

                totalColaboradores++;
                if (totalColaboradores % 1000 == 0) {
                    log.debug("FHIR: {} colaboradores procesados", totalColaboradores);
                }
            }

            bundle.put("entry", entries);
            bundle.put("total", totalRecursos);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, bundle);
            out.flush();

        } catch (Exception e) {
            log.error("Error al generar FHIR Bundle: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("HL7_FHIR_JSON",
                    "Error al generar el Bundle FHIR", e);
        }

        long tiempoMs = System.currentTimeMillis() - inicio;
        log.info("FHIR Bundle generado: {} colaboradores, {} recursos en {} ms",
                totalColaboradores, totalRecursos, tiempoMs);

        return ResultadoSerializacionHelper.construir(
                FormatoSalida.HL7_FHIR_JSON, totalColaboradores, tiempoMs, null);
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.HL7_FHIR_JSON;
    }
}
