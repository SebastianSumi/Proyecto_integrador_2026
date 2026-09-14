package pe.edu.upeu.saludablemente.notificacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "NOTIFICACIONES", schema = "SALUD_PERSONAL")
public class NotificacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICACION")
    private Long id;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "MODULO_ORIGEN", nullable = false, length = 50)
    private String moduloOrigen;

    @Column(name = "REFERENCIA_ID")
    private Long referenciaId;

    @Column(nullable = false)
    private Boolean leido = false;

    @Column(name = "FECHA_ENVIO", nullable = false)
    private LocalDateTime fechaEnvio;
}