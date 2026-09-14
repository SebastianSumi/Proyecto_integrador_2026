package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoAccionResponseDTO {

    private Long idAccion;
    private String codigoTipificado;
    private String descripcion;
    private Boolean requiereSeguimiento;
    private Integer diasSeguimiento;
}
