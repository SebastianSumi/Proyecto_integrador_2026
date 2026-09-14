package pe.edu.upeu.saludablemente.aptitudfisica.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePruebaFisicaDto {

    private Long idDetalleAptitud;

    @NotNull
    private Long idPrueba;

    private String nombrePrueba;

    @NotNull
    private BigDecimal valorObtenido;

    @NotNull
    private BigDecimal puntajeParcial;
}