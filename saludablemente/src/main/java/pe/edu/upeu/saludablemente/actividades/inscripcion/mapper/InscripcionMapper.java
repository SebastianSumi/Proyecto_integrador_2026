package pe.edu.upeu.saludablemente.actividades.inscripcion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionRequest;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

@Mapper(componentModel = "spring")
public interface InscripcionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "inscritaEn", ignore = true)
    @Mapping(target = "canceladaEn", ignore = true)
    Inscripcion toEntity(InscripcionRequest request);

    InscripcionResponse toResponse(Inscripcion inscripcion);
}
