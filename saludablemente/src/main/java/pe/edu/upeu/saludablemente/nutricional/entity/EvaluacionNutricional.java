package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evaluacion_nutricional")
public class EvaluacionNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long id;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @Column(name = "periodo_semestral", nullable = false, length = 20)
    private String periodoSemestral;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_evaluacion", nullable = false, length = 30)
    private EstadoEvaluacionNutricional estadoEvaluacion;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @OneToOne(mappedBy = "evaluacionNutricional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DetalleAntropometrico detalleAntropometrico;

    @OneToOne(mappedBy = "evaluacionNutricional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DetalleBioquimico detalleBioquimico;
}