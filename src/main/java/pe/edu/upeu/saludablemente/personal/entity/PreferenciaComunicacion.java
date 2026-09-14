package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "preferencia_comunicacion", schema = "SLB_PERSONAL")
public class PreferenciaComunicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preferencia")
    private Long id;

    @Column(name = "canal_preferido", nullable = false, length = 50)
    private String canalPreferido;

    @Column(name = "horario_contacto_inicio")
    private LocalTime horarioContactoInicio;

    @Column(name = "horario_contacto_fin")
    private LocalTime horarioContactoFin;

    @Column(name = "acepta_recordatorios", nullable = false)
    private Boolean aceptaRecordatorios;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false, unique = true)
    private Persona persona;
}