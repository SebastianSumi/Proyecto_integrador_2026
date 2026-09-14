package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaRequestDTO {

    @NotNull(message = "El ID de la persona es obligatorio")
    private Long idPersona;

    private String nombrePaciente;

    private Long idEvaluacionOrigen;

    private Integer edad;

    private String sexo;

    @NotEmpty(message = "Debe proporcionar al menos un detalle de indicador alterado")
    @Valid
    private List<AlertaDetalleRequestDTO> detalles;
}
