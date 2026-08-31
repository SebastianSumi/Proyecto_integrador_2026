package pe.edu.upeu.saludablemente.personal.persona.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaRequest;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaResponse;
import pe.edu.upeu.saludablemente.personal.persona.entity.Persona;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;
import pe.edu.upeu.saludablemente.personal.team.mapper.TeamMapper;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface PersonaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "team", source = "team")
    Persona toEntity(PersonaRequest request, Team team);

    @Mapping(target = "team", source = "team")
    @Mapping(target = "edad", ignore = true)
    PersonaResponse toResponse(Persona persona);
}
