package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliticaLongitudinalDTO {
    private BigDecimal deltaImcSemestreAnterior;
    private BigDecimal deltaGrasaVisceralSemestreAnterior;
    private BigDecimal deltaGlucosaSemestreAnterior;
    private String tendenciaMetabolica;
    private List<PuntoHistoricoDTO> puntosHistoricos;
}
