package pe.edu.upeu.saludablemente.teams.team.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void acceptsAValidTeamRequest() {
        TeamRequest request = new TeamRequest();
        request.setName("Wellness team");
        request.setDescription("Coordinates wellness activities");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsBlankNameAndValuesBeyondTheDocumentedLimits() {
        TeamRequest request = new TeamRequest();
        request.setName(" ");
        request.setDescription("x".repeat(201));

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(invalidProperties.contains("name"));
        assertTrue(invalidProperties.contains("description"));

        request.setName("x".repeat(61));
        request.setDescription(null);

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")));
    }

    @Test
    void serializesOnlyTheDocumentedResponseFields() throws Exception {
        TeamResponse response = TeamResponse.builder()
                .id(7L)
                .name("Wellness team")
                .description("Coordinates wellness activities")
                .active(true)
                .build();

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertEquals(Set.of("id", "name", "description", "active"), fieldNames(json));
        assertEquals(7L, json.get("id").longValue());
        assertEquals("Wellness team", json.get("name").textValue());
        assertEquals("Coordinates wellness activities", json.get("description").textValue());
        assertTrue(json.get("active").booleanValue());
    }

    @Test
    void keepsPersistenceAndStateFieldsOutOfTheRequestContract() {
        assertFalse(java.util.Arrays.stream(TeamRequest.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("id") || field.getName().equals("active")));
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new java.util.HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
