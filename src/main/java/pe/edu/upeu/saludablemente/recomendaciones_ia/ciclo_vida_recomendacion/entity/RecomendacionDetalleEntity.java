package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.config.JsonType;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.TipoSeccionRecomendacion;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "RECOMENDACION_IA_DETALLE", schema = "SALUD_RECOMENDACIONES_IA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DETALLE", columnDefinition = "RAW(16)")
    private UUID idDetalle;

    @ManyToOne
    @JoinColumn(name = "ID_RECOMENDACION", nullable = false)
    private RecomendacionIAEntity recomendacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_SECCION", nullable = false, length = 20)
    private TipoSeccionRecomendacion tipoSeccion;

    @Type(JsonType.class)
    @Column(name = "CONTENIDO_ESTRUCTURADO", columnDefinition = "CLOB")
    private Map<String, Object> contenidoEstructurado;

    @Column(name = "CONTRADICCIONES", columnDefinition = "CLOB")
    private String contraindicaciones;
}