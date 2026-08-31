package pe.edu.upeu.saludablemente.personal.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;
import pe.edu.upeu.saludablemente.personal.team.mapper.TeamMapper;
import pe.edu.upeu.saludablemente.personal.team.repository.TeamRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> listar() {
        return teamRepository.findAll().stream().map(teamMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse obtener(Long id) {
        return teamMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public TeamResponse crear(TeamRequest request) {
        Team team = teamMapper.toEntity(request);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse actualizar(Long id, TeamRequest request) {
        Team team = buscarOFallar(id);
        team.setNombre(request.getNombre());
        team.setDescripcion(request.getDescripcion());
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        teamRepository.delete(buscarOFallar(id));
    }

    private Team buscarOFallar(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team no encontrado: " + id));
    }
}
