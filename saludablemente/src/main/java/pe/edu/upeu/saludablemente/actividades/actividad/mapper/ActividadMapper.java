package pe.edu.upeu.saludablemente.actividades.actividad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;

@Mapper(componentModel = "spring")
public interface ActividadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Actividad toEntity(ActividadRequest request);

    ActividadResponse toResponse(Actividad actividad);
}
