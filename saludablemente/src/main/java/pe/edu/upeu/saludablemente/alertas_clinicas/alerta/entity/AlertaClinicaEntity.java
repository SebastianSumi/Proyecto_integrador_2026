package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ALERTA_CLINICA", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_ALERTA", columnDefinition = "RAW(16)")
    private UUID idAlerta;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "NOMBRE_PACIENTE", length = 150)
    private String nombrePaciente;

    @Column(name = "ID_EVALUACION_ORIGEN")
    private Long idEvaluacionOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "NIVEL_SEVERIDAD", nullable = false, length = 20)
    private NivelSeveridad nivelSeveridad;

    @Column(name = "SCORE_RIESGO")
    private Integer scoreRiesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoAlerta estado;

    @Lob
    @Column(name = "SNAPSHOT_INMUTABLE", columnDefinition = "CLOB")
    private String snapshotInmutable;

    @CreationTimestamp
    @Column(name = "FECHA_GENERACION", updatable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "FECHA_VENCIMIENTO_SLA")
    private LocalDateTime fechaVencimientoSla;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<AlertaClinicaDetalleEntity> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "alerta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ResolucionClinicaEntity resolucion;

    public void addDetalle(AlertaClinicaDetalleEntity detalle) {
        detalles.add(detalle);
        detalle.setAlerta(this);
    }

    public boolean isVencida() {
        if (fechaVencimientoSla == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaVencimientoSla) &&
                (estado == EstadoAlerta.PENDIENTE || estado == EstadoAlerta.EN_REVISION);
    }

    public boolean isAtendida() {
        return estado == EstadoAlerta.ATENDIDA || estado == EstadoAlerta.DESESTIMADA;
    }
}
