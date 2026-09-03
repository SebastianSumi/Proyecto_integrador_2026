package pe.edu.upeu.saludablemente.teams.team.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamStateRequest;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;
import pe.edu.upeu.saludablemente.teams.team.mapper.TeamMapper;
import pe.edu.upeu.saludablemente.teams.team.repository.TeamRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public List<TeamResponse> findAll(Boolean active) {
        List<Team> teams = active == null
                ? teamRepository.findAll()
                : teamRepository.findAllByActive(active);
        return teams.stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    public TeamResponse findById(Long id) {
        return teamMapper.toResponse(findTeam(id));
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        String name = request.name().trim();
        if (teamRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A team with this name already exists");
        }

        Team team = teamMapper.toEntity(request);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = findTeam(id);
        String name = request.name().trim();
        if (teamRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("A team with this name already exists");
        }

        teamMapper.updateEntity(team, request);
        return teamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse updateState(Long id, TeamStateRequest request) {
        Team team = findTeam(id);
        team.setActive(request.active());
        return teamMapper.toResponse(team);
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
