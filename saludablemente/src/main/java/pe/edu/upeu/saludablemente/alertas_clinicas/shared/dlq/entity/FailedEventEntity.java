package pe.edu.upeu.saludablemente.alertas_clinicas.shared.dlq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoDlq;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "DLQ_ALERTAS_FALLIDAS", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_FALLO", columnDefinition = "RAW(16)")
    private UUID idFallo;

    @Column(name = "PAYLOAD_EVENTO", nullable = false, columnDefinition = "CLOB")
    private String payloadEvento;

    @Column(name = "TIPO_EXCEPCION", nullable = false, length = 150)
    private String tipoExcepcion;

    @Column(name = "CONTADOR_REINTENTOS")
    @Builder.Default
    private Integer contadorReintentos = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    @Builder.Default
    private EstadoDlq estado = EstadoDlq.PENDIENTE;

    @CreationTimestamp
    @Column(name = "FECHA_FALLO", updatable = false)
    private LocalDateTime fechaFallo;

    @Column(name = "ULTIMO_INTENTO")
    private LocalDateTime ultimoIntento;

    @Column(name = "MENSAJE_ERROR", length = 500)
    private String mensajeError;

    public void incrementarReintentos() {
        this.contadorReintentos++;
        this.ultimoIntento = LocalDateTime.now();
    }

    public boolean isFatal() {
        return this.contadorReintentos >= 5;
    }
}