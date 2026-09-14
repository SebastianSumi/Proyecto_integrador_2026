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
public class AntropometriaDTO {
    private Double pesoKg;
    private Double tallaCm;
    private Double imc;
    private String diagnosticoImc;
    private Double perimetroAbdominalCm;
    private String diagnosticoPerimetro;
}
