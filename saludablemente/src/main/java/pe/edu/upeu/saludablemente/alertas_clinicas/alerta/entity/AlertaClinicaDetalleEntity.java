package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "ALERTA_CLINICA_DETALLE", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DETALLE", columnDefinition = "RAW(16)")
    private UUID idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ALERTA", nullable = false)
    private AlertaClinicaEntity alerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_INDICADOR", nullable = false, length = 40)
    private TipoIndicador tipoIndicador;

    @Column(name = "VALOR_MEDIDO", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMedido;

    @Column(name = "LIMITE_REFERENCIA", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteReferencia;

    @Column(name = "PORCENTAJE_DESVIACION", precision = 5, scale = 2)
    private BigDecimal porcentajeDesviacion;

    @Column(name = "MULTIPLICADOR_REINCIDENCIA", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal multiplicadorReincidencia = BigDecimal.ONE;

    public void calcularDesviacion() {
        if (valorMedido != null && limiteReferencia != null && limiteReferencia.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diferencia = valorMedido.subtract(limiteReferencia);
            this.porcentajeDesviacion = diferencia.divide(limiteReferencia, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
