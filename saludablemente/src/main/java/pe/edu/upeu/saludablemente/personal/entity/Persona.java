package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPersona;

    private String nombres;

    private String apellidoPaterno;

    private String apellidoMaterno;

    @Column(unique = true)
    private String celular;

    private LocalDate fechaNacimiento;

    private String sexo;

    private String tallaPolo;

    private Long idTeam;

    private Boolean activo;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PreferenciaComunicacion preferenciaComunicacion;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CredencialPrograma> credenciales = new ArrayList<>();
}