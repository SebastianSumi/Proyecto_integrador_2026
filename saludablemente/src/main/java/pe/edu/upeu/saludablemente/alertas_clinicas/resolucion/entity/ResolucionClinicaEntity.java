package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RESOLUCION_CLINICA", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolucionClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_RESOLUCION", columnDefinition = "RAW(16)")
    private UUID idResolucion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ALERTA", unique = true, nullable = false)
    private AlertaClinicaEntity alerta;

    @Column(name = "ID_USUARIO_EVALUADOR", nullable = false)
    private Long idUsuarioEvaluador;

    @Column(name = "NOMBRE_EVALUADOR", length = 150)
    private String nombreEvaluador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ACCION_CORRECTIVA")
    private CatalogoAccionCorrectivaEntity accionCorrectiva;

    @Column(name = "MOTIVO_DESESTIMACION", length = 100)
    private String motivoDesestimacion;

    @Lob
    @Column(name = "OBSERVACIONES_CLINICAS", columnDefinition = "CLOB")
    private String observacionesClinicas;

    @CreationTimestamp
    @Column(name = "FECHA_ATENCION", updatable = false)
    private LocalDateTime fechaAtencion;

    @Column(name = "ES_SEGUIMIENTO_PROGRAMADO")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Builder.Default
    private Boolean esSeguimientoProgramado = false;

    @Column(name = "FECHA_SEGUIMIENTO_PROGRAMADO")
    private LocalDateTime fechaSeguimientoProgramado;

    @Column(name = "DIAS_SEGUIMIENTO")
    private Integer diasSeguimiento;

    public boolean isSeguimientoProgramado() {
        return Boolean.TRUE.equals(esSeguimientoProgramado);
    }
}
