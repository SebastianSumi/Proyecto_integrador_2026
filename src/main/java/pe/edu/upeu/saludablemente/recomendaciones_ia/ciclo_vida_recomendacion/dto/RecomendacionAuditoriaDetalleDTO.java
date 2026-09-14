package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.EstadoInferencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionAuditoriaDetalleDTO {

    private UUID idRecomendacion;
    private Long idPersona;
    private Long idEvaluacion;
    private String modeloIaUsado;
    private Integer scoreConfianzaIa;
    private LocalDateTime fechaGeneracion;
    private String promptContexto;
    private Integer tiempoInferenciaMs;
    private Boolean vigente;
    private EstadoInferencia estadoInferencia;
}