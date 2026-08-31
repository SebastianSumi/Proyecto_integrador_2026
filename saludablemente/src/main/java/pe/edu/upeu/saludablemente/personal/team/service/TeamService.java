package pe.edu.upeu.saludablemente.personal.team.service;

import pe.edu.upeu.saludablemente.personal.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResponse;
import java.util.List;

public interface TeamService {
    List<TeamResponse> listar();
    TeamResponse obtener(Long id);
    TeamResponse crear(TeamRequest request);
    TeamResponse actualizar(Long id, TeamRequest request);
    void eliminar(Long id);
}
