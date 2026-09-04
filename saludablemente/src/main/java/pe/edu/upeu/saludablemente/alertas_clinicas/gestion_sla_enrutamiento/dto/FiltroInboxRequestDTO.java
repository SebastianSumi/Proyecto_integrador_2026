package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroInboxRequestDTO {

    private TipoIndicador tipoIndicador;
    private NivelSeveridad nivelSeveridad;
    private EstadoAlerta estado;
    private Long idTeam;
    private Long idEvaluador;
    private String nombrePaciente;
    private Boolean soloVencidos;
    private Integer pagina;
    private Integer limite;

    // Valores por defecto
    public int getPagina() {
        return pagina != null ? pagina : 0;
    }

    public int getLimite() {
        return limite != null ? limite : 15;
    }
}