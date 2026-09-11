package pe.edu.upeu.saludablemente.personal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PersonaRequestDto {

    @NotNull
    private Long idTeam;

    @NotBlank
    @Size(max = 80)
    private String nombres;

    @NotBlank
    @Size(max = 60)
    private String apellidoPaterno;

    @Size(max = 60)
    private String apellidoMaterno;

    @NotBlank
    @Size(max = 15)
    private String celular;

    @NotNull
    @Past
    private LocalDate fechaNacimiento;

    @NotBlank
    @Pattern(regexp = "[MF]", message = "sexo debe ser 'M' o 'F'")
    private String sexo;

    @Size(max = 5)
    private String tallaPolo;
}