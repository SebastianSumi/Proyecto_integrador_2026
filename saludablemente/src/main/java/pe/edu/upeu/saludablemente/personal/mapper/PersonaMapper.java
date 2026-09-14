package pe.edu.upeu.saludablemente.personal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.personal.dto.CredencialProgramaDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.dto.PreferenciaComunicacionDto;
import pe.edu.upeu.saludablemente.personal.entity.CredencialPrograma;
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.entity.PreferenciaComunicacion;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "preferenciaComunicacion", ignore = true)
    @Mapping(target = "credenciales", ignore = true)
    Persona toEntity(PersonaRequestDto request);

    @Mapping(target = "idPersona", source = "id")
    PersonaResponseDto toResponse(Persona persona);

    @Mapping(target = "idPreferencia", source = "id")
    PreferenciaComunicacionDto toPreferenciaComunicacionDto(PreferenciaComunicacion preferencia);

    @Mapping(target = "idCredencial", source = "id")
    CredencialProgramaDto toCredencialProgramaDto(CredencialPrograma credencial);
}