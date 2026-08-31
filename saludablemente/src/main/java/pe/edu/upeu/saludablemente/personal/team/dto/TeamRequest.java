package pe.edu.upeu.saludablemente.personal.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRequest {

    @NotBlank
    @Size(max = 60)
    private String nombre;

    @Size(max = 200)
    private String descripcion;
}
