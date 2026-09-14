package pe.edu.upeu.saludablemente.personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponseDto {

    private Long idPersona;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String celular;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String tallaPolo;
    private Long idTeam;
    private Boolean activo;
    private PreferenciaComunicacionDto preferenciaComunicacion;
}