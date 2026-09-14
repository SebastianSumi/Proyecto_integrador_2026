package pe.edu.upeu.saludablemente.perfil_reporte.dto;

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
public class AptitudFisicaResumenDTO {
    private Integer abdominales1Min;
    private Integer planchas1Min;
    private BigDecimal saltoSinImpulsoCm;
    private Integer saltoSogaReps;
    private BigDecimal carrera400mSegundos;
    private String nivelAptitud;
}
