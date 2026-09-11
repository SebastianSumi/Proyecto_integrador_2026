package pe.edu.upeu.saludablemente.personal.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.saludablemente.personal.dto.CredencialProgramaDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.dto.PreferenciaComunicacionDto;
import pe.edu.upeu.saludablemente.personal.entity.CredencialPrograma;
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.entity.PreferenciaComunicacion;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    Persona toEntity(PersonaRequestDto request);

    PersonaResponseDto toResponse(Persona persona);

    PreferenciaComunicacionDto toPreferenciaComunicacionDto(PreferenciaComunicacion preferencia);

    CredencialProgramaDto toCredencialProgramaDto(CredencialPrograma credencial);
}