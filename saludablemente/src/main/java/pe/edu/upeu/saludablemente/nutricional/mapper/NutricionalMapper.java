package pe.edu.upeu.saludablemente.nutricional.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleAntropometricoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleAntropometrico;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleBioquimico;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;

@Mapper(componentModel = "spring")
public interface NutricionalMapper {

    DetalleAntropometrico toAntropometrico(DetalleAntropometricoDto dto);

    DetalleAntropometricoDto toAntropometricoDto(DetalleAntropometrico detalle);

    DetalleBioquimico toBioquimico(DetalleBioquimicoDto dto);

    DetalleBioquimicoDto toBioquimicoDto(DetalleBioquimico detalle);

    EvaluacionNutricional toEntity(EvaluacionNutricionalRequestDto request);

    EvaluacionNutricionalResponseDto toResponse(EvaluacionNutricional evaluacion);
}