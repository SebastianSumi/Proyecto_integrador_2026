package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.FormatoNoSoportadoException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class ParquetDatasetWriter implements SerializadorFormato {

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        log.warn("Formato APACHE_PARQUET aun no implementado.");
        throw new FormatoNoSoportadoException("APACHE_PARQUET");
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.APACHE_PARQUET;
    }
}
