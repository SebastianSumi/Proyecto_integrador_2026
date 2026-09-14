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
public class RecomendacionVigenteDTO {
    private String idRecomendacion;
    private boolean vigente;
    private String enfoquePrincipal;
    private Integer frecuenciaSemanalDias;
    private String estrategiaNutricional;
}
