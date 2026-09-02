package pe.edu.upeu.saludablemente.teams.team.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;

@Component
public class TeamMapper {

    public Team toEntity(TeamRequest request) {
        return new Team(normalize(request.name()), normalizeDescription(request.description()));
    }

    public TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getDescription(), team.isActive());
    }

    public void updateEntity(Team team, TeamRequest request) {
        team.update(normalize(request.name()), normalizeDescription(request.description()));
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeDescription(String value) {
        return value == null ? null : value.trim();
    }
}