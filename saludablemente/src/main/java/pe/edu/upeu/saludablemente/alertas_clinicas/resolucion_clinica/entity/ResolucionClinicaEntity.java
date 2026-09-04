package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;

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

    @OneToOne
    @JoinColumn(name = "ID_ALERTA", unique = true, nullable = false)
    private AlertaClinicaEntity alerta;

    @Column(name = "ID_USUARIO_EVALUADOR", nullable = false)
    private Long idUsuarioEvaluador;

    @ManyToOne
    @JoinColumn(name = "ID_ACCION_CORRECTIVA")
    private CatalogoAccionCorrectivaEntity accionCorrectiva;

    @Column(name = "MOTIVO_DESESTIMACION", length = 100)
    private String motivoDesestimacion;

    @Column(name = "OBSERVACIONES_CLINICAS", columnDefinition = "CLOB")
    private String observacionesClinicas;

    @CreationTimestamp
    @Column(name = "FECHA_ATENCION", updatable = false)
    private LocalDateTime fechaAtencion;
}