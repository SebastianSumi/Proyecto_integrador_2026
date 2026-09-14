package pe.edu.upeu.saludablemente.auditoria.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.dto.GeoLocationDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.SesionUsuarioEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;
import pe.edu.upeu.saludablemente.auditoria.service.GeoIpService;
import pe.edu.upeu.saludablemente.auditoria.service.SesionUsuarioService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImpossibleTravelDetector implements AnomaliaDetector {

    private static final double VELOCIDAD_COMERCIAL_MAX_KMH = 900.0;
    private static final long VENTANA_MAXIMA_HORAS = 24;

    private final GeoIpService geoIpService;
    private final SesionUsuarioService sesionUsuarioService;

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.IMPOSSIBLE_TRAVEL;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getUsuario() != null
                && !"anonimo".equals(contexto.getUsuario())
                && contexto.getDireccionIp() != null;
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        try {
            Optional<SesionUsuarioEntity> ultimaSesion =
                    sesionUsuarioService.obtenerUltimaSesion(contexto.getUsuario());

            if (ultimaSesion.isEmpty()) {
                return noDetectada();
            }

            SesionUsuarioEntity anterior = ultimaSesion.get();
            String ipAnterior = anterior.getDireccionIp();
            String ipActual = contexto.getDireccionIp();

            if (ipActual == null || ipActual.equals(ipAnterior)) {
                return noDetectada();
            }

            GeoLocationDTO ubicacionAnterior = anterior.getLatitud() != null
                    ? construirDesdeEntidad(anterior)
                    : geoIpService.resolverUbicacion(ipAnterior);

            GeoLocationDTO ubicacionActual = geoIpService.resolverUbicacion(ipActual);

            if (!ubicacionAnterior.isUbicacionValida() || !ubicacionActual.isUbicacionValida()) {
                return noDetectada();
            }

            LocalDateTime ahora = contexto.getTimestamp() != null
                    ? contexto.getTimestamp() : LocalDateTime.now();
            long minutosTranscurridos = Duration.between(anterior.getFechaLogin(), ahora).toMinutes();

            if (minutosTranscurridos < 1 || minutosTranscurridos > VENTANA_MAXIMA_HORAS * 60) {
                return noDetectada();
            }

            double distanciaKm = calcularDistanciaHaversine(
                    ubicacionAnterior.getLatitud(), ubicacionAnterior.getLongitud(),
                    ubicacionActual.getLatitud(), ubicacionActual.getLongitud());

            if (distanciaKm < 100) {
                return noDetectada();
            }

            double horas = minutosTranscurridos / 60.0;
            double velocidadRequerida = distanciaKm / horas;

            if (velocidadRequerida > VELOCIDAD_COMERCIAL_MAX_KMH) {
                return construirAlerta(contexto, anterior, ubicacionAnterior,
                        ubicacionActual, distanciaKm, minutosTranscurridos, velocidadRequerida);
            }

            return noDetectada();

        } catch (Exception e) {
            log.error("Error al evaluar ImpossibleTravel: {}", e.getMessage(), e);
            return noDetectada();
        }
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(ContextoDeteccion contexto,
                                                      SesionUsuarioEntity anterior,
                                                      GeoLocationDTO ubicacionAnterior,
                                                      GeoLocationDTO ubicacionActual,
                                                      double distanciaKm,
                                                      long minutos,
                                                      double velocidadKmh) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ipPrevia", anterior.getDireccionIp());
        metadata.put("ipActual", contexto.getDireccionIp());
        metadata.put("ubicacionPrevia", ubicacionAnterior.getCiudad() + ", " + ubicacionAnterior.getPais());
        metadata.put("ubicacionActual", ubicacionActual.getCiudad() + ", " + ubicacionActual.getPais());
        metadata.put("distanciaKm", Math.round(distanciaKm));
        metadata.put("deltaTiempoMinutos", minutos);
        metadata.put("velocidadCalculadaKmH", Math.round(velocidadKmh));

        log.error("IMPOSSIBLE TRAVEL detectado para usuario {}: {} km en {} minutos",
                contexto.getUsuario(), Math.round(distanciaKm), minutos);

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.IMPOSSIBLE_TRAVEL)
                .severidad(SeveridadAnomalia.CRITICAL)
                .usuarioAfectado(contexto.getUsuario())
                .mensaje("Salto geografico imposible: " + ubicacionAnterior.getCiudad()
                        + " -> " + ubicacionActual.getCiudad())
                .metadataContexto(metadata)
                .build();
    }

    private GeoLocationDTO construirDesdeEntidad(SesionUsuarioEntity entidad) {
        return GeoLocationDTO.builder()
                .direccionIp(entidad.getDireccionIp())
                .ciudad(entidad.getCiudad())
                .pais(entidad.getPais())
                .codigoPais(entidad.getCodigoPais())
                .latitud(entidad.getLatitud())
                .longitud(entidad.getLongitud())
                .ubicacionValida(entidad.getLatitud() != null && entidad.getLongitud() != null)
                .build();
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double RADIO_TIERRA_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.IMPOSSIBLE_TRAVEL)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }
}
