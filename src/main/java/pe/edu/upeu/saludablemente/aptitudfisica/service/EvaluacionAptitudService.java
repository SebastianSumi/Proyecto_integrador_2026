package pe.edu.upeu.saludablemente.aptitudfisica.service;

import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface EvaluacionAptitudService {

    List<EvaluacionAptitudResponseDto> listar(Long personaId, Boolean sincronizado, LocalDate desde, LocalDate hasta);

    List<EvaluacionAptitudResumenDto> listarResumen(Long personaId, Boolean sincronizado, LocalDate desde, LocalDate hasta);

    EvaluacionAptitudAgregadoDto obtenerAgregados(Long personaId);

    EvaluacionAptitudResponseDto obtener(Long idEvaluacionAptitud);

    EvaluacionAptitudResponseDto registrarEvaluacion(EvaluacionAptitudRequestDto request);

    void eliminar(Long idEvaluacionAptitud);
}