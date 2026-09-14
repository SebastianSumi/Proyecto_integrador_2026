package pe.edu.upeu.saludablemente.perfil_reporte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "COLA_GENERACION_REPORTES", schema = "SALUD_PERFIL_REPORTE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaGeneracionReportesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TAREA", columnDefinition = "RAW(16)")
    private UUID idTarea;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "PERIODO_SEMESTRAL", nullable = false, length = 10)
    private String periodoSemestral;

    @Column(name = "FORZAR_REGENERACION", nullable = false)
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Builder.Default
    private Boolean forzarRegeneracion = false;

    @Column(name = "INTENTOS_ACUMULADOS", nullable = false)
    @Builder.Default
    private Integer intentosAcumulados = 0;

    @Column(name = "ESTADO_COLA", nullable = false, length = 20)
    @Builder.Default
    private String estadoCola = "EN_COLA";

    @Lob
    @Column(name = "ULTIMO_ERROR", columnDefinition = "CLOB")
    private String ultimoError;

    @CreationTimestamp
    @Column(name = "FECHA_SOLICITUD", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @UpdateTimestamp
    @Column(name = "FECHA_ULTIMA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;
}
