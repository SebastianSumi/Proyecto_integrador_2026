package pe.edu.upeu.saludablemente.asistencia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ASISTENCIAS", schema = "SALUD_PERSONAL", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"ID_ACTIVIDAD", "ID_PERSONA"}))
public class AsistenciaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ID_ACTIVIDAD", nullable = false)
    private Long idActividad; // Cabecera (Actividad)

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona; // Detalle (Persona)

    @Column(name = "FECHA_HORA_MARCADO", nullable = false)
    private LocalDateTime fechaHoraMarcado;

    @Column(nullable = false, length = 30)
    private String metodo; // Ejemplo: QR, WEB, OFFLINE_SYNC

    @Column(nullable = false, length = 20)
    private String estado; // Ejemplo: REGISTRADO, TARDANZA

    @Column(name = "ES_OFFLINE", nullable = false)
    private Boolean esOffline = false;

    @Column(name = "DISPOSITIVO_UUID", length = 100)
    private String dispositivoUuid; // Para evitar duplicados en sincronización
}