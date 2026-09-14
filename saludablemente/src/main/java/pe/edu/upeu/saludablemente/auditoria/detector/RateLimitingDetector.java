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
public class RateLimitingDetector implements AnomaliaDetector {

    private static final int UMBRAL_PETICIONES = 100;
    private static final long VENTANA_SEGUNDOS = 60;

    private final Map<String, VentanaPeticiones> peticionesPorUsuario = new ConcurrentHashMap<>();

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.RATE_LIMIT_EXCEEDED;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getUsuario() != null
                && !"anonimo".equals(contexto.getUsuario());
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        String usuario = contexto.getUsuario();
        LocalDateTime ahora = contexto.getTimestamp() != null
                ? contexto.getTimestamp() : LocalDateTime.now();

        VentanaPeticiones ventana = peticionesPorUsuario.computeIfAbsent(
                usuario, k -> new VentanaPeticiones());

        synchronized (ventana) {
            ventana.limpiarAntiguas(ahora);
            ventana.registrar(ahora);

            if (ventana.contar() >= UMBRAL_PETICIONES) {
                return construirAlerta(contexto, ventana.contar());
            }
        }

        return noDetectada();
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(ContextoDeteccion contexto, int total) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalPeticiones", total);
        metadata.put("ventanaSegundos", VENTANA_SEGUNDOS);
        metadata.put("umbral", UMBRAL_PETICIONES);
        metadata.put("direccionIp", contexto.getDireccionIp());

        log.warn("RATE LIMIT EXCEEDED detectado para usuario {}: {} peticiones en {} segundos",
                contexto.getUsuario(), total, VENTANA_SEGUNDOS);

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.RATE_LIMIT_EXCEEDED)
                .severidad(SeveridadAnomalia.WARNING)
                .usuarioAfectado(contexto.getUsuario())
                .mensaje("Exceso de peticiones: " + total + " en " + VENTANA_SEGUNDOS + " segundos")
                .metadataContexto(metadata)
                .build();
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.RATE_LIMIT_EXCEEDED)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }

    private static class VentanaPeticiones {
        private final Deque<LocalDateTime> peticiones = new ArrayDeque<>();

        void registrar(LocalDateTime momento) {
            peticiones.addLast(momento);
        }

        void limpiarAntiguas(LocalDateTime ahora) {
            LocalDateTime limite = ahora.minusSeconds(VENTANA_SEGUNDOS);
            while (!peticiones.isEmpty() && peticiones.peekFirst().isBefore(limite)) {
                peticiones.pollFirst();
            }
        }

        int contar() {
            return peticiones.size();
        }
    }
}
