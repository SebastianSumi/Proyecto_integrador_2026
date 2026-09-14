package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPacienteResponseDTO {
    private String tipoIndicador;
    private LocalDateTime fechaGeneracion;
    private String mensajeAmigable;
    private String estadoAtencion;
}
