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
public class PuntoHistoricoDTO {
    private String periodo;
    private Double imc;
    private Double grasaVisceral;
    private Double glucosaMgDl;
    private Double colesterolTotalMgDl;
}
