package pe.edu.upeu.saludablemente.perfil_reporte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import pe.edu.upeu.saludablemente.perfil_reporte.enums.EstadoGeneracionReporte;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.TipoAlmacenamiento;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "REPORTE_PERSONAL", schema = "SALUD_PERFIL_REPORTE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportePersonalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_REPORTE", columnDefinition = "RAW(16)")
    private UUID idReporte;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "NOMBRE_COLABORADOR", length = 150)
    private String nombreColaborador;

    @Column(name = "PERIODO_SEMESTRAL", nullable = false, length = 10)
    private String periodoSemestral;

    @Column(name = "VERSION_REPORTE", nullable = false)
    @Builder.Default
    private Integer versionReporte = 1;

    @Lob
    @Column(name = "SNAPSHOT_DATOS", nullable = false, columnDefinition = "CLOB")
    private String snapshotDatos;

    @Column(name = "RUTA_ALMACENAMIENTO", nullable = false, length = 300)
    private String rutaAlmacenamiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_ALMACENAMIENTO", nullable = false, length = 20)
    @Builder.Default
    private TipoAlmacenamiento tipoAlmacenamiento = TipoAlmacenamiento.LOCAL;

    @Column(name = "TAMANO_BYTES", nullable = false)
    private Long tamanoBytes;

    @Column(name = "HASH_INTEGRIDAD_SHA256", nullable = false, length = 64)
    private String hashIntegridadSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_GENERACION", nullable = false, length = 20)
    @Builder.Default
    private EstadoGeneracionReporte estadoGeneracion = EstadoGeneracionReporte.COMPLETADO;

    @CreationTimestamp
    @Column(name = "FECHA_GENERACION", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "GENERADO_POR", nullable = false, length = 80)
    private String generadoPor;

    @Column(name = "VIGENTE")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Builder.Default
    private Boolean vigente = true;
}
