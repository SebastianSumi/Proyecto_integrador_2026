package pe.edu.upeu.saludablemente.perfil_reporte.entity;

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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "REPORTE_DESCARGA_BITACORA", schema = "SALUD_PERFIL_REPORTE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDescargaBitacoraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DESCARGA", columnDefinition = "RAW(16)")
    private UUID idDescarga;

    @Column(name = "ID_REPORTE", nullable = false)
    private UUID idReporte;

    @Column(name = "ID_PERSONA_SOLICITANTE", nullable = false)
    private Long idPersonaSolicitante;

    @Column(name = "DIRECCION_IP", nullable = false, length = 45)
    private String direccionIp;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "FECHA_DESCARGA", nullable = false, updatable = false)
    private LocalDateTime fechaDescarga;
}
