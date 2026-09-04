package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "PERSONA", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaEntity {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @Column(name = "FECHA_NACIMIENTO", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "SEXO", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String sexo;

    @Column(name = "AREA_TRABAJO", length = 100)
    private String areaTrabajo;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public Integer getEdad() {
        if (fechaNacimiento != null) {
            return LocalDate.now().getYear() - fechaNacimiento.getYear();
        }
        return null;
    }
}