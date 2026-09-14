package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.stream.Stream;

public interface ExtraccionPoblacionalService {

    long contarPoblacion(FiltroPoblacionalParams filtros);

    Stream<CohorteClinicaDTO> extraerPoblacionStream(FiltroPoblacionalParams filtros);
}
