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
public class AnaliticaHistoricaDTO {
    private Double deltaImcSemestreAnterior;
    private Double deltaGrasaVisceralSemestreAnterior;
    private String tendenciaMetabolica;
    private List<PuntoHistoricoDTO> puntosHistoricos;
    private String semaforoBienestarGlobal;
}
