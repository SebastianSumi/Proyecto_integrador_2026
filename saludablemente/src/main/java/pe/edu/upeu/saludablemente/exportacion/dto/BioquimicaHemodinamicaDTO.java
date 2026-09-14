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
public class BioquimicaHemodinamicaDTO {
    private Double glucosaMgDl;
    private Double colesterolTotalMgDl;
    private Double colesterolHdlMgDl;
    private Double colesterolLdlMgDl;
    private Double trigliceridosMgDl;
    private Integer presionSistolica;
    private Integer presionDiastolica;
}
