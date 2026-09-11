package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "evaluacion_nutricional")
public class EvaluacionNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvaluacion;

    private Long idPersona;

    private LocalDate fechaEvaluacion;

    private String periodoSemestral;

    private String estadoEvaluacion;

    private String observaciones;

    @OneToOne(mappedBy = "evaluacionNutricional", cascade = CascadeType.ALL, orphanRemoval = true)
    private DetalleAntropometrico detalleAntropometrico;

    @OneToOne(mappedBy = "evaluacionNutricional", cascade = CascadeType.ALL, orphanRemoval = true)
    private DetalleBioquimico detalleBioquimico;
}