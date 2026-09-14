package pe.edu.upeu.saludablemente.actividades.actividad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

@Mapper(componentModel = "spring")
public interface ActividadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "inscripciones", ignore = true)
    Actividad toEntity(ActividadRequest request);

    ActividadResponse toResponse(Actividad actividad);

    ActividadDetalleResponse toDetalleResponse(Actividad actividad);

    InscripcionDetalleResponse toDetalleResponse(Inscripcion inscripcion);
}
