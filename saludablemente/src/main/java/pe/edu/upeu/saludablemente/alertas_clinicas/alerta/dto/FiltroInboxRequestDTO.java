package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto;

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
    private Long idPersona;
    private Long idTeam;

    @Builder.Default
    private Integer pagina = 0;

    @Builder.Default
    private Integer limite = 15;
}
