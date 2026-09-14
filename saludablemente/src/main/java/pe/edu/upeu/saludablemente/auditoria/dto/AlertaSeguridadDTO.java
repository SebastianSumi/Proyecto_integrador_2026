package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoAlertaSeguridad;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSeguridadDTO {
    private UUID idAlerta;
    private String usuarioAfectado;
    private TipoAnomalia tipoAnomalia;
    private SeveridadAnomalia severidad;
    private Map<String, Object> metadataContexto;
    private EstadoAlertaSeguridad estadoAlerta;
    private LocalDateTime fechaDeteccion;
}
