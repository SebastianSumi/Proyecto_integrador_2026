package pe.edu.upeu.saludablemente.metas.meta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Public representation of a health goal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaResponse {

    private Long id;
    private Long personaId;
    private String tipoMeta;
    private String descripcion;
    private BigDecimal valorObjetivo;
    private BigDecimal valorActual;
    private LocalDate fechaInicio;
    private LocalDate fechaLimite;
    private EstadoMeta estado;
}