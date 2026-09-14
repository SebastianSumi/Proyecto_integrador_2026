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
public class WebhookPayloadDTO {

    private UUID idTarea;
    private String tituloLote;
    private String formato;
    private Long totalRegistros;
    private String hashSha256;
    private String urlDescarga;
    private LocalDateTime fechaCompletado;
}
