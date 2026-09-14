package pe.edu.upeu.saludablemente.teams.team.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamMapperTest {

    private final TeamMapper mapper = Mappers.getMapper(TeamMapper.class);

    @Test
    void mapsRequestFieldsAndPreservesEntityDefaults() {
        TeamRequest request = new TeamRequest();
        request.setName("Wellness team");
        request.setDescription("Coordinates wellness activities");

        Team team = mapper.toEntity(request);

        assertEquals("Wellness team", team.getName());
        assertEquals("Coordinates wellness activities", team.getDescription());
        assertNull(team.getId());
        assertTrue(team.isActive());
    }

    @Test
    void preservesNullRequestDescriptionAndEntityDefaults() {
        TeamRequest request = new TeamRequest();
        request.setName("Operations team");
        request.setDescription(null);

        Team team = mapper.toEntity(request);

        assertEquals("Operations team", team.getName());
        assertNull(team.getDescription());
        assertNull(team.getId());
        assertTrue(team.isActive());
    }

    @Test
    void mapsAllEntityFieldsToResponse() {
        Team team = new Team();
        team.setId(7L);
        team.setName("Wellness team");
        team.setDescription("Coordinates wellness activities");
        team.setActive(false);

        TeamResponse response = mapper.toResponse(team);

        assertEquals(7L, response.getId());
        assertEquals("Wellness team", response.getName());
        assertEquals("Coordinates wellness activities", response.getDescription());
        assertEquals(false, response.isActive());
    }

    @Test
    void preservesNullEntityDescriptionAndActiveStateInResponse() {
        Team team = new Team();
        team.setId(8L);
        team.setName("Operations team");
        team.setDescription(null);
        team.setActive(true);

        TeamResponse response = mapper.toResponse(team);

        assertEquals(8L, response.getId());
        assertEquals("Operations team", response.getName());
        assertNull(response.getDescription());
        assertTrue(response.isActive());
    }
}
