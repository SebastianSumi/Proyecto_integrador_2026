package pe.edu.upeu.saludablemente.nutricional.service;

import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalAgregadoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResumenDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.entity.EstadoEvaluacionNutricional;

import java.time.LocalDate;
import java.util.List;

public interface EvaluacionNutricionalService {

    List<EvaluacionNutricionalResponseDto> listar(Long personaId,
                                                  EstadoEvaluacionNutricional estado,
                                                  String periodoSemestral,
                                                  LocalDate desde,
                                                  LocalDate hasta);

    List<EvaluacionNutricionalResumenDto> listarResumen(Long personaId,
                                                        EstadoEvaluacionNutricional estado,
                                                        String periodoSemestral,
                                                        LocalDate desde,
                                                        LocalDate hasta);

    EvaluacionNutricionalAgregadoDto obtenerAgregados(Long personaId, EstadoEvaluacionNutricional estado);

    EvaluacionNutricionalResponseDto obtener(Long idEvaluacion);

    EvaluacionNutricionalResponseDto registrarAntropometria(EvaluacionNutricionalRequestDto request);

    EvaluacionNutricionalResponseDto vincularBioquimico(Long idEvaluacion, DetalleBioquimicoDto detalleBioquimico);

    void eliminar(Long idEvaluacion);
}