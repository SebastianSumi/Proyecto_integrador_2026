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
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoAlertaSeguridad;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BITACORA_SEGURIDAD", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSeguridadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_ALERTA", columnDefinition = "RAW(16)")
    private UUID idAlerta;

    @Column(name = "USUARIO_AFECTADO", length = 100)
    private String usuarioAfectado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_ANOMALIA", nullable = false, length = 50)
    private TipoAnomalia tipoAnomalia;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERIDAD", nullable = false, length = 20)
    private SeveridadAnomalia severidad;

    @Lob
    @Column(name = "METADATA_CONTEXTO", columnDefinition = "CLOB")
    private String metadataContexto;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_ALERTA", nullable = false, length = 30)
    @Builder.Default
    private EstadoAlertaSeguridad estadoAlerta = EstadoAlertaSeguridad.NO_RESUELTA;

    @Column(name = "FECHA_DETECCION", nullable = false)
    private LocalDateTime fechaDeteccion;

    @Column(name = "FECHA_RESOLUCION")
    private LocalDateTime fechaResolucion;

    @Column(name = "RESUELTO_POR", length = 100)
    private String resueltoPor;

    @Column(name = "OBSERVACIONES", length = 500)
    private String observaciones;
}
