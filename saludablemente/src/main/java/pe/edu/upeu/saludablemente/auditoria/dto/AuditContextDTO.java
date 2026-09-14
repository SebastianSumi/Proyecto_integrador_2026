package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditContextDTO {
    private String usuario;
    private String roles;
    private String tenantId;
    private String sessionId;
    private String direccionIp;
    private String puertoRemoto;
    private String userAgent;
    private String uri;
    private String metodoHttp;
}
