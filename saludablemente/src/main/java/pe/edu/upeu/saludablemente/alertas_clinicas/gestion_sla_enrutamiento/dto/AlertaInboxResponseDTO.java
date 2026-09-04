package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaInboxResponseDTO {

    private UUID idAlerta;
    private String nombrePaciente;
    private String areaTrabajo;
    private TipoIndicador tipoIndicador;
    private NivelSeveridad nivelSeveridad;
    private EstadoAlerta estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaVencimientoSla;
    private Long horasSlaRestantes;
    private String especialidadAsignada;
    private Long idEvaluadorAsignado;
    private Boolean esVencida;

}