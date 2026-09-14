package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_antropometrico",  schema = "SLB_NUTRICIONAL")
public class DetalleAntropometrico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_antropometrico")
    private Long id;

    @Column(name = "estatura_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal estaturaCm;

    @Column(name = "peso_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "perimetro_abdominal_cm", precision = 5, scale = 2)
    private BigDecimal perimetroAbdominalCm;

    @Column(name = "imc", nullable = false, precision = 4, scale = 2)
    private BigDecimal imc;

    @Column(name = "porcentaje_grasa", precision = 4, scale = 2)
    private BigDecimal porcentajeGrasa;

    @Column(name = "porcentaje_masa_muscular", precision = 4, scale = 2)
    private BigDecimal porcentajeMasaMuscular;

    @Column(name = "porcentaje_grasa_visceral", precision = 4, scale = 2)
    private BigDecimal porcentajeGrasaVisceral;

    @Column(name = "dx_imc", length = 50)
    private String dxImc;

    @Column(name = "dx_perimetro_abdominal", length = 50)
    private String dxPerimetroAbdominal;

    @Column(name = "dx_grasa", length = 50)
    private String dxGrasa;

    @Column(name = "dx_masa_muscular", length = 50)
    private String dxMasaMuscular;

    @Column(name = "dx_grasa_visceral", length = 50)
    private String dxGrasaVisceral;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false, unique = true)
    private EvaluacionNutricional evaluacionNutricional;
}