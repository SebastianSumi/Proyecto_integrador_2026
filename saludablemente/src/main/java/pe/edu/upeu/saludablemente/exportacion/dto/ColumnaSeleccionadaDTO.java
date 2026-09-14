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
public class ColumnaSeleccionadaDTO {

    private String claveTecnica;
    private String nombreLegible;
    private String unidadMedida;
    private boolean activa;
}
