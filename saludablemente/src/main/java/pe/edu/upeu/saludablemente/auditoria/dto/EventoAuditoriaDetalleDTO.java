package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAuditoriaDetalleDTO {
    private UUID idBitacora;
    private Long secuencia;
    private String tipoEvento;
    private String entidadAfectada;
    private String idEntidad;
    private String tipoOperacion;
    private Long idPersona;
    private String usuarioAutor;
    private String rolUsuario;
    private String direccionIp;
    private String userAgent;
    private String endpointHttp;
    private String metodoHttp;
    private Integer codigoRespuestaHttp;
    private String hashRegistro;
    private String hashAnterior;
    private Boolean fueraHorarioLaboral;
    private LocalDateTime fechaRegistro;
    private List<CambioAtomicoViewDTO> cambios;
    private Boolean integridadVerificada;
    private Map<String, Object> snapshotAnterior;
    private Map<String, Object> snapshotPosterior;
}
