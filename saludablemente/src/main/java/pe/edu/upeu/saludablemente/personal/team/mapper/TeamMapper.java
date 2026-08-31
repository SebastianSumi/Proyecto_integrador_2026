package pe.edu.upeu.saludablemente.personal.team.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResumen;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Team toEntity(TeamRequest request);

    TeamResponse toResponse(Team team);

    TeamResumen toResumen(Team team);
}
