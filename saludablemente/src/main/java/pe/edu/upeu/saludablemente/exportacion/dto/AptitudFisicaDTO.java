package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudFisicaDTO {
    private Integer abdominales1Min;
    private Integer planchas1Min;
    private Double saltoSinImpulsoCm;
    private Integer carrera400mSegundos;
    private String nivelAptitud;
}
