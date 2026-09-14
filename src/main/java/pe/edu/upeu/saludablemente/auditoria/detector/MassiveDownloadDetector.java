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
public class MassiveDownloadDetector implements AnomaliaDetector {

    private static final int UMBRAL_DESCARGAS = 10;
    private static final long VENTANA_SEGUNDOS = 60;

    private final Map<String, VentanaDescargas> ventanasPorUsuario = new ConcurrentHashMap<>();

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.MASSIVE_DOWNLOAD;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getUsuario() != null
                && contexto.getUri() != null
                && contexto.getUri().contains("/descargar");
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        String usuario = contexto.getUsuario();
        LocalDateTime ahora = contexto.getTimestamp() != null
                ? contexto.getTimestamp() : LocalDateTime.now();

        VentanaDescargas ventana = ventanasPorUsuario.computeIfAbsent(
                usuario, k -> new VentanaDescargas());

        synchronized (ventana) {
            ventana.limpiarAntiguas(ahora);
            ventana.registrar(ahora);

            if (ventana.contar() >= UMBRAL_DESCARGAS) {
                return construirAlerta(contexto, ventana.contar(), ahora);
            }
        }

        return noDetectada();
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(ContextoDeteccion contexto,
                                                      int total,
                                                      LocalDateTime ahora) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalDescargas", total);
        metadata.put("ventanaSegundos", VENTANA_SEGUNDOS);
        metadata.put("umbral", UMBRAL_DESCARGAS);
        metadata.put("direccionIp", contexto.getDireccionIp());

        log.error("MASSIVE DOWNLOAD detectado para usuario {}: {} descargas en {} segundos",
                contexto.getUsuario(), total, VENTANA_SEGUNDOS);

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.MASSIVE_DOWNLOAD)
                .severidad(SeveridadAnomalia.CRITICAL)
                .usuarioAfectado(contexto.getUsuario())
                .mensaje("Descarga masiva detectada: " + total + " archivos en "
                        + VENTANA_SEGUNDOS + " segundos")
                .metadataContexto(metadata)
                .build();
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.MASSIVE_DOWNLOAD)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }

    private static class VentanaDescargas {
        private final Deque<LocalDateTime> descargas = new ArrayDeque<>();

        void registrar(LocalDateTime momento) {
            descargas.addLast(momento);
        }

        void limpiarAntiguas(LocalDateTime ahora) {
            LocalDateTime limite = ahora.minusSeconds(VENTANA_SEGUNDOS);
            while (!descargas.isEmpty() && descargas.peekFirst().isBefore(limite)) {
                descargas.pollFirst();
            }
        }

        int contar() {
            return descargas.size();
        }
    }
}
