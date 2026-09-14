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
public class HttpAuditMetadataDTO {
    private String direccionIp;
    private String puertoRemoto;
    private String userAgent;
    private String uri;
    private String metodoHttp;
    private Integer codigoRespuesta;
    private Long tiempoRespuestaMs;
}
