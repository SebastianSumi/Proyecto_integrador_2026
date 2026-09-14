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
public class NivelPrivacidadAplicadoDTO {

    private String modoPrivacidad;
    private boolean identificadoresSuprimidos;
    private boolean cuasiIdentificadoresGeneralizados;
    private Integer registrosSuprimidosPorKAnonimity;
    private Integer kAnonimityAplicado;
}
