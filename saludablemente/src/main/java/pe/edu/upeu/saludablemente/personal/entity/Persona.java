package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PERSONA", schema = "SLB_PERSONAL")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "celular", nullable = false, unique = true, length = 15)
    private String celular;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 10)
    private Sexo sexo;

    @Column(name = "talla_polo", length = 10)
    private String tallaPolo;

    @Column(name = "id_team")
    private Long idTeam;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PreferenciaComunicacion preferenciaComunicacion;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CredencialPrograma> credenciales = new ArrayList<>();
}