package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.EstadoInferencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "RECOMENDACION_IA", schema = "SALUD_RECOMENDACIONES_IA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionIAEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_RECOMENDACION", columnDefinition = "RAW(16)")
    private UUID idRecomendacion;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "ID_EVALUACION")
    private Long idEvaluacion;

    @Column(name = "MODELO_IA_USADO", length = 60)
    private String modeloIaUsado;

    @Column(name = "PROMPT_CONTEXTO", columnDefinition = "CLOB")
    private String promptContexto;

    @Column(name = "SCORE_CONFIANZA_IA")
    private Integer scoreConfianzaIa;

    @Column(name = "VIGENTE")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Builder.Default
    private Boolean vigente = true;

    @Column(name = "ESTADO_INFERENCIA")
    @Enumerated(EnumType.STRING)
    private EstadoInferencia estadoInferencia;

    @CreationTimestamp
    @Column(name = "FECHA_GENERACION", updatable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "FECHA_LECTURA_COLABORADOR")
    private LocalDateTime fechaLecturaColaborador;

    @Column(name = "LEIDA")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "TIEMPO_INFERENCIA_MS")
    private Integer tiempoInferenciaMs;

    @OneToMany(mappedBy = "recomendacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RecomendacionDetalleEntity> detalles = new ArrayList<>();

    public void addDetalle(RecomendacionDetalleEntity detalle) {
        detalles.add(detalle);
        detalle.setRecomendacion(this);
    }

    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLecturaColaborador = LocalDateTime.now();
    }

    public boolean isActiva() {
        return Boolean.TRUE.equals(vigente);
    }
}