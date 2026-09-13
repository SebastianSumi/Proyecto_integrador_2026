package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_bioquimico")
public class DetalleBioquimico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bioquimico")
    private Long id;

    @Column(name = "glucosa", precision = 5, scale = 2)
    private BigDecimal glucosa;

    @Column(name = "colesterol", precision = 5, scale = 2)
    private BigDecimal colesterol;

    @Column(name = "trigliceridos", precision = 5, scale = 2)
    private BigDecimal trigliceridos;

    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    @Column(name = "dx_bioquimico", length = 100)
    private String dxBioquimico;

    @Column(name = "archivo_origen", length = 255)
    private String archivoOrigen;

    @Column(name = "fecha_importacion")
    private LocalDateTime fechaImportacion;

    @Column(name = "id_usuario_importador")
    private Long idUsuarioImportador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = true, unique = true)
    private EvaluacionNutricional evaluacionNutricional;
}