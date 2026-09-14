package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgresoExportacionSseDTO {

    private UUID idTarea;
    private Integer porcentajeAvance;
    private String faseActual;
    private Long registrosProcesados;
    private Long totalRegistros;
    private Long tiempoEstimadoRestanteSegundos;
    private String mensajeDetalle;
}
