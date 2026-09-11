package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalle_antropometrico")
public class DetalleAntropometrico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAntropometrico;

    private BigDecimal estaturaCm;

    private BigDecimal pesoKg;

    private BigDecimal perimetroAbdominalCm;

    private BigDecimal imc;

    private BigDecimal porcentajeGrasa;

    private BigDecimal porcentajeMasaMuscular;

    private BigDecimal porcentajeGrasaVisceral;

    private String dxImc;

    private String dxPerimetroAbdominal;

    private String dxGrasa;

    private String dxMasaMuscular;

    private String dxGrasaVisceral;

    @OneToOne
    @JoinColumn(name = "id_evaluacion")
    private EvaluacionNutricional evaluacionNutricional;
}