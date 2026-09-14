package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.util.stream.Stream;

public interface FormatDispatcherService {

    ResultadoSerializacionDTO despachar(Stream<CohorteClinicaDTO> datos,
                                         FormatoSalida formato,
                                         OpcionesExportacionDTO opciones,
                                         OutputStream out);
}
