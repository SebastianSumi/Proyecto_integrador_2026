package pe.edu.upeu.saludablemente.nutricional.service;

import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;

import java.util.List;

public interface EvaluacionNutricionalService {

    List<EvaluacionNutricionalResponseDto> listar();

    EvaluacionNutricionalResponseDto obtener(Long idEvaluacion);

    List<EvaluacionNutricionalResponseDto> listarPorPersona(Long idPersona);

    EvaluacionNutricionalResponseDto registrarAntropometria(EvaluacionNutricionalRequestDto request);

    EvaluacionNutricionalResponseDto vincularBioquimico(Long idEvaluacion, DetalleBioquimicoDto detalleBioquimico);

    void eliminar(Long idEvaluacion);
}