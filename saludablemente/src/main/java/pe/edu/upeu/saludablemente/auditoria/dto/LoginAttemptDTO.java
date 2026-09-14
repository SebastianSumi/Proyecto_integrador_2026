package pe.edu.upeu.saludablemente.auditoria.dto;

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
public class LoginAttemptDTO {
    private String usuario;
    private String direccionIp;
    private LocalDateTime timestamp;
    private boolean exitoso;
    private String userAgent;
}
