package pe.edu.upeu.saludablemente.aptitudfisica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionAptitudResumenDto {

    private Long idEvaluacionAptitud;
    private Long idPersona;
    private LocalDate fechaRegistro;
    private BigDecimal puntajeGlobal;
    private String diagnosticoAptitud;
    private Boolean sincronizado;
    private Integer cantidadDetalles;
}