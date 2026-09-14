package pe.edu.upeu.saludablemente.notificacion.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionDto {
    private Long id;
    private Long idPersona;
    private String titulo;
    private String mensaje;
    private String moduloOrigen;
    private Long referenciaId;
    private Boolean leido;
    private LocalDateTime fechaEnvio;
}