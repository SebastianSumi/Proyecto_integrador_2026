package pe.edu.upeu.saludablemente.personal.persona.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PersonaRequest {

    @NotBlank
    @Size(max = 80)
    private String nombres;

    @NotBlank
    @Size(max = 60)
    private String apellidoPaterno;

    @Size(max = 60)
    private String apellidoMaterno;

    @Size(max = 60)
    private String areaTrabajo;

    @NotBlank
    @Size(max = 15)
    private String celular;

    @NotNull
    private LocalDate fechaNacimiento;

    @Size(max = 5)
    private String tallaPolo;

    @Size(max = 20)
    private String estadoCivil;

    @Size(max = 30)
    private String nivelEducativo;

    @NotBlank
    @Pattern(regexp = "[MF]")
    private String sexo;

    @NotNull
    private Long teamId;
}
