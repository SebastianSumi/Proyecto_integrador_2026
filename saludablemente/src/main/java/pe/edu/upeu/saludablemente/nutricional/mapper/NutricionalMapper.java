package pe.edu.upeu.saludablemente.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleAntropometricoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleAntropometrico;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleBioquimico;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;

@Mapper(componentModel = "spring")
public interface NutricionalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "evaluacionNutricional", ignore = true)
    DetalleAntropometrico toAntropometrico(DetalleAntropometricoDto dto);

    @Mapping(target = "idAntropometrico", source = "id")
    DetalleAntropometricoDto toAntropometricoDto(DetalleAntropometrico detalle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "evaluacionNutricional", ignore = true)
    DetalleBioquimico toBioquimico(DetalleBioquimicoDto dto);

    @Mapping(target = "idBioquimico", source = "id")
    DetalleBioquimicoDto toBioquimicoDto(DetalleBioquimico detalle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalleBioquimico", ignore = true)
    EvaluacionNutricional toEntity(EvaluacionNutricionalRequestDto request);

    @Mapping(target = "idEvaluacion", source = "id")
    EvaluacionNutricionalResponseDto toResponse(EvaluacionNutricional evaluacion);
}