package pe.edu.upeu.saludablemente.personal.persona.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResumen;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponse {
    private Long id;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String areaTrabajo;
    private String celular;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String tallaPolo;
    private String estadoCivil;
    private String nivelEducativo;
    private String sexo;
    private Boolean activo;
    private TeamResumen team;
}
