package pe.edu.upeu.saludablemente.teams.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;
import pe.edu.upeu.saludablemente.teams.team.mapper.TeamMapper;
import pe.edu.upeu.saludablemente.teams.team.repository.TeamRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository repository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private TeamServiceImpl service;

    @Test
    void findsAllTeamsWhenActiveFilterIsNull() {
        Team activeTeam = team(1L, "Wellness", "Wellness team", true);
        Team inactiveTeam = team(2L, "Operations", null, false);
        TeamResponse activeResponse = response(1L, "Wellness", "Wellness team", true);
        TeamResponse inactiveResponse = response(2L, "Operations", null, false);

        when(repository.findAll()).thenReturn(List.of(activeTeam, inactiveTeam));
        when(mapper.toResponse(activeTeam)).thenReturn(activeResponse);
        when(mapper.toResponse(inactiveTeam)).thenReturn(inactiveResponse);

        List<TeamResponse> result = service.findAll(null);

        assertEquals(List.of(activeResponse, inactiveResponse), result);
        verify(repository).findAll();
        verify(repository, never()).findAllByActive(any(Boolean.class));
    }

    @Test
    void filtersTeamsByActiveStateWhenFilterIsPresent() {
        Team activeTeam = team(1L, "Wellness", "Wellness team", true);
        TeamResponse response = response(1L, "Wellness", "Wellness team", true);

        when(repository.findAllByActive(true)).thenReturn(List.of(activeTeam));
        when(mapper.toResponse(activeTeam)).thenReturn(response);

        List<TeamResponse> result = service.findAll(true);

        assertEquals(List.of(response), result);
        verify(repository).findAllByActive(true);
        verify(repository, never()).findAll();
    }

    @Test
    void returnsTeamById() {
        Team team = team(7L, "Wellness", "Wellness team", true);
        TeamResponse response = response(7L, "Wellness", "Wellness team", true);

        when(repository.findById(7L)).thenReturn(Optional.of(team));
        when(mapper.toResponse(team)).thenReturn(response);

        assertEquals(response, service.findById(7L));
    }

    @Test
    void failsWhenTeamDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));

        assertEquals("Team with id 99 was not found", exception.getMessage());
    }

    @Test
    void declaresReadOnlyDefaultAndWriteTransactions() throws Exception {
        Transactional classTransaction = TeamServiceImpl.class.getAnnotation(Transactional.class);

        assertTrue(classTransaction.readOnly());
        assertFalse(TeamServiceImpl.class.getMethod("create", TeamRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(TeamServiceImpl.class.getMethod("update", Long.class, TeamRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(TeamServiceImpl.class.getMethod("changeState", Long.class, boolean.class)
                .getAnnotation(Transactional.class).readOnly());
    }

    @Test
    void createsTeamFromRequest() {
        TeamRequest request = request("Wellness", "Wellness team");
        Team mapped = team(null, "Wellness", "Wellness team", true);
        Team saved = team(10L, "Wellness", "Wellness team", true);
        TeamResponse response = response(10L, "Wellness", "Wellness team", true);

        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.create(request));
    }

    @Test
    void updatesExistingTeam() {
        TeamRequest request = request("Updated", "Updated team");
        Team existing = team(10L, "Old", "Old team", true);
        Team saved = team(10L, "Updated", "Updated team", true);
        TeamResponse response = response(10L, "Updated", "Updated team", true);

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.update(10L, request));
        assertEquals("Updated", existing.getName());
        assertEquals("Updated team", existing.getDescription());
    }

    @Test
    void changesTeamState() {
        Team existing = team(10L, "Wellness", "Wellness team", true);
        Team saved = team(10L, "Wellness", "Wellness team", false);
        TeamResponse response = response(10L, "Wellness", "Wellness team", false);

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.changeState(10L, false));
        assertFalse(existing.isActive());
    }

    private TeamRequest request(String name, String description) {
        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private Team team(Long id, String name, String description, boolean active) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setDescription(description);
        team.setActive(active);
        return team;
    }

    private TeamResponse response(Long id, String name, String description, boolean active) {
        return TeamResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .active(active)
                .build();
    }
}
