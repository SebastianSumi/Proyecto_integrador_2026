package pe.edu.upeu.saludablemente.nutricional.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalle_bioquimico")
public class DetalleBioquimico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBioquimico;

    private BigDecimal glucosa;

    private BigDecimal colesterol;

    private BigDecimal trigliceridos;

    private Integer presionSistolica;

    private Integer presionDiastolica;

    private String dxBioquimico;

    private String archivoOrigen;

    private LocalDateTime fechaImportacion;

    private Long idUsuarioImportador;

    @OneToOne
    @JoinColumn(name = "id_evaluacion", nullable = true)
    private EvaluacionNutricional evaluacionNutricional;
}