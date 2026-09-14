package pe.edu.upeu.saludablemente.auditoria.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BreakGlassDetector implements AnomaliaDetector {

    private static final Set<String> ENTIDADES_SENSIBLES = Set.of(
            "EVALUACION_NUTRICIONAL",
            "APTITUD_FISICA",
            "ALERTA_CLINICA",
            "REPORTE_PERSONAL"
    );

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.BREAK_GLASS_ACCESS;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getUsuario() != null
                && contexto.getUri() != null;
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        if (esAccesoNoAsignado(contexto)) {
            return construirAlerta(contexto);
        }
        return noDetectada();
    }

    private boolean esAccesoNoAsignado(ContextoDeteccion contexto) {
        String uri = contexto.getUri();
        if (uri == null) {
            return false;
        }
        return uri.contains("/break-glass")
                || uri.contains("/acceso-emergencia")
                || uri.contains("/override");
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(ContextoDeteccion contexto) {
        LocalDateTime ahora = contexto.getTimestamp() != null
                ? contexto.getTimestamp() : LocalDateTime.now();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("usuario", contexto.getUsuario());
        metadata.put("entidadAfectada", contexto.getTipoEntidad());
        metadata.put("idEntidad", contexto.getIdEntidad());
        metadata.put("idPersona", contexto.getIdPersona());
        metadata.put("uri", contexto.getUri());
        metadata.put("direccionIp", contexto.getDireccionIp());
        metadata.put("timestamp", ahora.toString());

        log.error("BREAK GLASS ACCESS detectado: usuario {} accedio a {}:{}",
                contexto.getUsuario(), contexto.getTipoEntidad(), contexto.getIdEntidad());

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.BREAK_GLASS_ACCESS)
                .severidad(SeveridadAnomalia.CRITICAL)
                .usuarioAfectado(contexto.getUsuario())
                .mensaje("Acceso de emergencia a expediente: "
                        + contexto.getTipoEntidad() + ":" + contexto.getIdEntidad())
                .metadataContexto(metadata)
                .build();
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.BREAK_GLASS_ACCESS)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }
}
