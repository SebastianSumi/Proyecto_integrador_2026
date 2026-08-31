package pe.edu.upeu.saludablemente.personal.persona.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;
import java.time.LocalDate;

@Entity
@Table(name = "PERSONAS", schema = "SALUD_PERSONAL")
@Getter
@Setter
@NoArgsConstructor
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRES", nullable = false, length = 80)
    private String nombres;

    @Column(name = "APELLIDO_PATERNO", nullable = false, length = 60)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 60)
    private String apellidoMaterno;

    @Column(name = "AREA_TRABAJO", length = 60)
    private String areaTrabajo;

    @Column(name = "CELULAR", nullable = false, length = 15)
    private String celular;

    @Column(name = "FECHA_NACIMIENTO", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "TALLA_POLO", length = 5)
    private String tallaPolo;

    @Column(name = "ESTADO_CIVIL", length = 20)
    private String estadoCivil;

    @Column(name = "NIVEL_EDUCATIVO", length = 30)
    private String nivelEducativo;

    @Column(name = "SEXO", nullable = false, length = 1)
    private String sexo;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TEAM", nullable = false)
    private Team team;
}
