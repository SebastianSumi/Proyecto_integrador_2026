package pe.edu.upeu.saludablemente.teams.team.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.observability.TransactionLog;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;
import pe.edu.upeu.saludablemente.teams.team.mapper.TeamMapper;
import pe.edu.upeu.saludablemente.teams.team.repository.TeamRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository repository;
    private final TeamMapper mapper;

    @Override
    public List<TeamResponse> findAll(Boolean active) {
        List<Team> teams = active == null
                ? repository.findAll()
                : repository.findAllByActive(active);

        return teams.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TeamResponse findById(Long id) {
        return mapper.toResponse(findTeam(id));
    }

    @Override
    @Transactional
    public TeamResponse create(TeamRequest request) {
        Team team = mapper.toEntity(request);
        TeamResponse response = mapper.toResponse(repository.save(team));
        TransactionLog.afterCommit(() -> log.info("team.created id={} active={}", response.getId(), response.isActive()));
        return response;
    }

    @Override
    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = findTeam(id);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        TeamResponse response = mapper.toResponse(repository.save(team));
        TransactionLog.afterCommit(() -> log.info("team.updated id={} active={}", response.getId(), response.isActive()));
        return response;
    }

    @Override
    @Transactional
    public TeamResponse changeState(Long id, boolean active) {
        Team team = findTeam(id);
        team.setActive(active);
        TeamResponse response = mapper.toResponse(repository.save(team));
        TransactionLog.afterCommit(() -> log.info("team.state_changed id={} active={}", response.getId(), response.isActive()));
        return response;
    }

    private Team findTeam(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team with id " + id + " was not found"));
    }
}
