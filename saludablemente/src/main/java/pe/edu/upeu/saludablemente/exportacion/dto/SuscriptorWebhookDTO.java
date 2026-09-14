package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscriptorWebhookDTO {

    private UUID idSuscriptor;
    private String nombreSistema;
    private String urlEndpoint;
    private List<String> eventosSuscritos;
    private boolean activo;
    private LocalDateTime fechaRegistro;
}
