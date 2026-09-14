package pe.edu.upeu.saludablemente.teams.team.service;

import java.util.List;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;

public interface TeamService {

    List<TeamResponse> findAll(Boolean active);

    TeamResponse findById(Long id);

    TeamResponse create(TeamRequest request);

    TeamResponse update(Long id, TeamRequest request);

    TeamResponse changeState(Long id, boolean active);
}
