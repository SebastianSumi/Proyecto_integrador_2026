package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesestimarAlertaRequestDTO {

    @NotNull(message = "El ID de la alerta es obligatorio")
    private UUID idAlerta;

    @NotBlank(message = "El motivo de desestimación es obligatorio")
    private String motivoDesestimacion;

    private String observacionesClinicas;
}