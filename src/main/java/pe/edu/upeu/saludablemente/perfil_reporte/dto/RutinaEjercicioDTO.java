package pe.edu.upeu.saludablemente.perfil_reporte.dto;

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
public class RutinaEjercicioDTO {
    private String enfoquePrincipal;
    private Integer frecuenciaSemanalDias;
    private List<BloqueEjercicioDTO> bloques;
    private List<String> contraindicaciones;
}
