package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPacienteResponseDTO {
    private UUID idAlerta;
    private Long idPersona;
    private String tipoIndicador;
    private String mensajeAmigable;
    private String estadoAtencion;
    private LocalDateTime fechaGeneracion;
    private Long diasRestantesSla;
}
