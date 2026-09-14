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
public class ComposicionCorporalDTO {
    private Double porcentajeGrasa;
    private String diagnosticoGrasa;
    private Double porcentajeMusculo;
    private String diagnosticoMusculo;
    private Double nivelGrasaVisceral;
    private String diagnosticoGrasaVisceral;
}
