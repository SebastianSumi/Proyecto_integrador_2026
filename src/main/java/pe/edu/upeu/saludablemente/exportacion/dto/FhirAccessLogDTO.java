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
public class FhirAccessLogDTO {

    private UUID idLog;
    private String idSistemaCliente;
    private String recursoSolicitado;
    private String parametrosConsulta;
    private Integer codigoHttpRespuesta;
    private String direccionIp;
    private Integer tiempoRespuestaMs;
    private LocalDateTime fechaPeticion;
}
