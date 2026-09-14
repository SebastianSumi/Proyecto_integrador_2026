package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaIniciadaResponseDTO {

    private UUID idTarea;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private Long totalRegistrosEstimados;
    private String endpointSseProgreso;
    private String mensaje;
}
