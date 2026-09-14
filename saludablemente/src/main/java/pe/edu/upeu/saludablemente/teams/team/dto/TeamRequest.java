package pe.edu.upeu.saludablemente.teams.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Input contract for creating or updating a team.
 */
@Getter
@Setter
public class TeamRequest {

    @NotBlank
    @Size(max = 60)
    private String name;

    @Size(max = 200)
    private String description;
}