package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDTO {
    private Long idPersona;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String sexo;
    private String areaTrabajo;
    private String email;
}