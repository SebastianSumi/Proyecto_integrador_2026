package pe.edu.upeu.saludablemente.teams.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamStateRequest;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;
import pe.edu.upeu.saludablemente.teams.team.mapper.TeamMapper;
import pe.edu.upeu.saludablemente.teams.team.repository.TeamRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void returnsOnlyTeamsMatchingRequestedState() {
        Team team = new Team("Nutrition", null);
        given(teamRepository.findAllByActive(true)).willReturn(List.of(team));
        given(teamMapper.toResponse(team)).willReturn(new TeamResponse(1L, "Nutrition", null, true));

        List<TeamResponse> result = teamService.findAll(true);

        assertThat(result).hasSize(1);
        then(teamRepository).should().findAllByActive(true);
        then(teamRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void appliesTheRequestedStateInsteadOfTogglingIt() {
        Team team = new Team("Nutrition", null);
        team.setActive(false);
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMapper.toResponse(team)).willReturn(new TeamResponse(1L, "Nutrition", null, true));

        TeamResponse response = teamService.updateState(1L, new TeamStateRequest(true));

        assertThat(team.isActive()).isTrue();
        assertThat(response.active()).isTrue();
    }
}
