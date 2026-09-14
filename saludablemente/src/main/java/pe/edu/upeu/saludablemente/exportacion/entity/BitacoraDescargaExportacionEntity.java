package pe.edu.upeu.saludablemente.exportacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BITACORA_DESCARGA_EXPORTACION", schema = "SALUD_EXPORTACION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraDescargaExportacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DESCARGA", columnDefinition = "RAW(16)")
    private UUID idDescarga;

    @Column(name = "ID_TAREA", nullable = false, columnDefinition = "RAW(16)")
    private UUID idTarea;

    @Column(name = "ID_USUARIO_DESCARGA", nullable = false)
    private Long idUsuarioDescarga;

    @Column(name = "DIRECCION_IP", nullable = false, length = 45)
    private String direccionIp;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @Column(name = "FECHA_DESCARGA", nullable = false)
    @Builder.Default
    private LocalDateTime fechaDescarga = LocalDateTime.now();
}
