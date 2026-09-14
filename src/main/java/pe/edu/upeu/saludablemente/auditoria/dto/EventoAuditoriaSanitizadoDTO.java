package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAuditoriaSanitizadoDTO {
    private String tipoEvento;
    private String tipoEntidad;
    private String idEntidad;
    private String tipoOperacion;
    private Long idPersona;
    private Map<String, Object> estadoAnterior;
    private Map<String, Object> estadoNuevo;
    private AuditContextDTO contextoHttp;
    private String usuarioAutor;
    private LocalDateTime timestampEvento;
}
