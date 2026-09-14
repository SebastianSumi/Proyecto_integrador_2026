package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto;

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
public class AtenderAlertaRequestDTO {

    @NotNull(message = "El ID de la alerta es obligatorio")
    private UUID idAlerta;

    @NotNull(message = "El ID de la acción correctiva es obligatorio")
    private Long idAccionCorrectiva;

    private String observacionesClinicas;

    private Long idUsuarioEvaluador;

    private String nombreEvaluador;
}
