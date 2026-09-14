package pe.edu.upeu.saludablemente.perfil_reporte.dto;

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
public class ColaboradorFiliacionDTO {
    private Long idPersona;
    private String codigoColaborador;
    private String nombreCompleto;
    private String areaTrabajo;
    private String sede;
    private Integer edad;
    private String sexo;
}
