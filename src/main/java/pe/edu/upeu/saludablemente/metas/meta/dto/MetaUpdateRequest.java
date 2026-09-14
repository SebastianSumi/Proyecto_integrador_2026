package pe.edu.upeu.saludablemente.metas.meta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input contract for changing the mutable fields of a health goal.
 */
@Getter
@Setter
public class MetaUpdateRequest {

    @NotBlank
    @Size(max = 40)
    private String tipoMeta;

    @Size(max = 200)
    private String descripcion;

    @NotNull
    @Positive
    private BigDecimal valorObjetivo;

    @PositiveOrZero
    private BigDecimal valorActual;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaLimite;
}