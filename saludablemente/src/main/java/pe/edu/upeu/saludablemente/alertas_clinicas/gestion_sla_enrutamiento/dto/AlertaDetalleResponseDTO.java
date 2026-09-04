package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDetalleResponseDTO {

    private UUID idAlerta;
    private Long idEvaluacionOrigen;
    private String nombrePaciente;
    private Integer edad;
    private String sexo;
    private NivelSeveridad nivelSeveridad;
    private Integer scoreRiesgo;
    private String sindromeDetectado;
    private List<IndicadorEvaluadoDTO> detallesClinicos;
    private EstadoAlerta estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaVencimientoSla;
    private Long horasSlaRestantes;
    private String especialidadAsignada;
    private Long idEvaluadorAsignado;
}