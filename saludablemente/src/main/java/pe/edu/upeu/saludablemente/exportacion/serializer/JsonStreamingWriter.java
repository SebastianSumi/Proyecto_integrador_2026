package pe.edu.upeu.saludablemente.exportacion.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
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
import java.util.Iterator;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonStreamingWriter implements SerializadorFormato {

    private final ObjectMapper objectMapper;

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        long inicio = System.currentTimeMillis();
        int totalRegistros = 0;

        try (JsonGenerator generator = objectMapper.getFactory().createGenerator(out)) {
            generator.useDefaultPrettyPrinter();
            generator.writeStartObject();

            generator.writeStringField("fechaGeneracion", LocalDateTime.now().toString());
            generator.writeBooleanField("incluirDiccionario", opciones != null && opciones.isIncluirDiccionario());
            generator.writeArrayFieldStart("colaboradores");

            Iterator<CohorteClinicaDTO> iterator = datos.iterator();
            while (iterator.hasNext()) {
                CohorteClinicaDTO cohorte = iterator.next();
                generator.writeObject(cohorte);
                totalRegistros++;

                if (totalRegistros % 5000 == 0) {
                    generator.flush();
                    log.debug("JSON: {} registros procesados", totalRegistros);
                }
            }

            generator.writeEndArray();
            generator.writeNumberField("totalRegistros", totalRegistros);
            generator.writeEndObject();
            generator.flush();

        } catch (Exception e) {
            log.error("Error al serializar JSON: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("JSON_ESTRUCTURADO", "Error al escribir el JSON", e);
        }

        long tiempoMs = System.currentTimeMillis() - inicio;
        log.info("JSON generado: {} registros en {} ms", totalRegistros, tiempoMs);

        return ResultadoSerializacionHelper.construir(
                FormatoSalida.JSON_ESTRUCTURADO, totalRegistros, tiempoMs, null);
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.JSON_ESTRUCTURADO;
    }
}
