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
 * Input contract for creating a health goal for a person.
 */
@Getter
@Setter
public class MetaCreateRequest {

    @NotNull
    @Positive
    private Long personaId;

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