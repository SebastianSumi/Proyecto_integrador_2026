package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoColaDlq;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EVENTO_AUDITORIA_DLQ", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAuditoriaDlqEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_FALLO", columnDefinition = "RAW(16)")
    private UUID idFallo;

    @Column(name = "TIPO_EVENTO", nullable = false, length = 80)
    private String tipoEvento;

    @Lob
    @Column(name = "PAYLOAD_EVENTO", nullable = false, columnDefinition = "CLOB")
    private String payloadEvento;

    @Column(name = "TIPO_ERROR", nullable = false, length = 120)
    private String tipoError;

    @Column(name = "MENSAJE_ERROR", length = 500)
    private String mensajeError;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private EstadoColaDlq estado = EstadoColaDlq.PENDIENTE_REINTENTO;

    @Column(name = "INTENTOS_REINTENTO", nullable = false)
    @Builder.Default
    private Integer intentosReintento = 0;

    @CreationTimestamp
    @Column(name = "FECHA_REGISTRO", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "FECHA_ULTIMO_INTENTO")
    private LocalDateTime fechaUltimoIntento;

    public void incrementarReintentos() {
        this.intentosReintento++;
        this.fechaUltimoIntento = LocalDateTime.now();
    }

    public boolean esFatal() {
        return this.intentosReintento >= 5;
    }
}
