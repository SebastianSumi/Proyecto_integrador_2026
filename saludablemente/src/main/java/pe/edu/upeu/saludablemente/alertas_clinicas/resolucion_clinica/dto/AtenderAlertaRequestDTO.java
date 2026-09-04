package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtenderAlertaRequestDTO {

    @NotNull(message = "El ID de la alerta es obligatorio")
    private UUID idAlerta;

    @NotNull(message = "La acción correctiva es obligatoria")
    private Long idAccionCorrectiva;

    private String observacionesClinicas;

    @NotNull(message = "El estado de resolución es obligatorio")
    private EstadoAlerta estadoResolucion; // Debe ser ATENDIDA o DESESTIMADA
}