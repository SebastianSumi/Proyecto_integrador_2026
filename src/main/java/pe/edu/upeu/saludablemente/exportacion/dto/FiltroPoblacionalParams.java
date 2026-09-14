package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPoblacionalParams {

    private List<String> periodos;
    private List<String> sedes;
    private List<String> departamentos;
    private List<String> clasificacionesRiesgo;
    private Integer edadMin;
    private Integer edadMax;
    private Boolean incluirHistorialCompleto;
    private List<String> columnasSeleccionadas;
}
