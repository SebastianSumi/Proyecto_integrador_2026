package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoAnomaliaDeteccionDTO {
    private boolean detectada;
    private TipoAnomalia tipoAnomalia;
    private SeveridadAnomalia severidad;
    private String usuarioAfectado;
    private String mensaje;
    private Map<String, Object> metadataContexto;
}
