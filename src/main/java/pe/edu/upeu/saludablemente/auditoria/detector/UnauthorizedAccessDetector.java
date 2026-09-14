package pe.edu.upeu.saludablemente.auditoria.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnauthorizedAccessDetector implements AnomaliaDetector {

    private static final int UMBRAL_INTENTOS = 5;
    private static final long VENTANA_MINUTOS = 10;

    private final Map<String, VentanaIntentos> intentosPorUsuario = new ConcurrentHashMap<>();

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.UNAUTHORIZED_ACCESS;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getUsuario() != null
                && contexto.getUri() != null
                && contexto.getDireccionIp() != null;
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        String usuario = contexto.getUsuario();
        LocalDateTime ahora = contexto.getTimestamp() != null
                ? contexto.getTimestamp() : LocalDateTime.now();

        VentanaIntentos ventana = intentosPorUsuario.computeIfAbsent(
                usuario, k -> new VentanaIntentos());

        synchronized (ventana) {
            ventana.limpiarAntiguas(ahora);

            if (esPeticionSospechosa(contexto)) {
                ventana.registrar(ahora);
            }

            if (ventana.contar() >= UMBRAL_INTENTOS) {
                return construirAlerta(contexto, ventana.contar());
            }
        }

        return noDetectada();
    }

    private boolean esPeticionSospechosa(ContextoDeteccion contexto) {
        String uri = contexto.getUri();
        if (uri == null) {
            return false;
        }
        return uri.contains("/admin")
                || uri.contains("/auditoria/forense")
                || uri.contains("/api/internal");
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(ContextoDeteccion contexto, int total) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalIntentos", total);
        metadata.put("ventanaMinutos", VENTANA_MINUTOS);
        metadata.put("umbral", UMBRAL_INTENTOS);
        metadata.put("direccionIp", contexto.getDireccionIp());
        metadata.put("ultimaUri", contexto.getUri());

        log.error("UNAUTHORIZED ACCESS detectado para usuario {}: {} intentos en {} minutos",
                contexto.getUsuario(), total, VENTANA_MINUTOS);

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.UNAUTHORIZED_ACCESS)
                .severidad(SeveridadAnomalia.CRITICAL)
                .usuarioAfectado(contexto.getUsuario())
                .mensaje("Intentos reiterados de acceso no autorizado: " + total)
                .metadataContexto(metadata)
                .build();
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.UNAUTHORIZED_ACCESS)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }

    private static class VentanaIntentos {
        private final Deque<LocalDateTime> intentos = new ArrayDeque<>();

        void registrar(LocalDateTime momento) {
            intentos.addLast(momento);
        }

        void limpiarAntiguas(LocalDateTime ahora) {
            LocalDateTime limite = ahora.minusMinutes(VENTANA_MINUTOS);
            while (!intentos.isEmpty() && intentos.peekFirst().isBefore(limite)) {
                intentos.pollFirst();
            }
        }

        int contar() {
            return intentos.size();
        }
    }
}
