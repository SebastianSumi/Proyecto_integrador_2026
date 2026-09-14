package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SindromeResultadoDTO {
    private String codigo;
    private String descripcion;
    private int puntosExtra;
}
