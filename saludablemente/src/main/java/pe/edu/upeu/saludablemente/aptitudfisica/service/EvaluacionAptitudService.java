package pe.edu.upeu.saludablemente.aptitudfisica.service;

import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;

import java.util.List;

public interface EvaluacionAptitudService {

    List<EvaluacionAptitudResponseDto> listar();

    EvaluacionAptitudResponseDto obtener(Long idEvaluacionAptitud);

    List<EvaluacionAptitudResponseDto> listarPorPersona(Long idPersona);

    EvaluacionAptitudResponseDto registrarEvaluacion(EvaluacionAptitudRequestDto request);

    void eliminar(Long idEvaluacionAptitud);
}