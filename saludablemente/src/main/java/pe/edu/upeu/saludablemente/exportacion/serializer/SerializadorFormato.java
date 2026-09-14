package pe.edu.upeu.saludablemente.exportacion.serializer;

import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.util.stream.Stream;

public interface SerializadorFormato {

    ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                          OpcionesExportacionDTO opciones,
                                          OutputStream out);

    FormatoSalida getFormatoSoportado();
}
