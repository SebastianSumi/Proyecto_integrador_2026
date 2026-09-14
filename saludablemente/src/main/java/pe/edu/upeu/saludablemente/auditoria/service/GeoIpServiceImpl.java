package pe.edu.upeu.saludablemente.auditoria.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.dto.GeoLocationDTO;

import java.net.InetAddress;
import java.util.Optional;

@Slf4j
@Service
public class GeoIpServiceImpl implements GeoIpService {

    private final DatabaseReader databaseReader;

    public GeoIpServiceImpl(@Autowired(required = false) @Qualifier("geoIpDatabaseReader") DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }

    @Override
    public GeoLocationDTO resolverUbicacion(String direccionIp) {
        if (!estaDisponible() || direccionIp == null || direccionIp.isBlank()) {
            return ubicacionNoDisponible(direccionIp);
        }

        if (esIpPrivada(direccionIp)) {
            return GeoLocationDTO.builder()
                    .direccionIp(direccionIp)
                    .ciudad("Red Interna")
                    .pais("Local")
                    .codigoPais("LO")
                    .latitud(0.0)
                    .longitud(0.0)
                    .ubicacionValida(false)
                    .build();
        }

        try {
            InetAddress ip = InetAddress.getByName(direccionIp);
            Optional<CityResponse> respuestaOpt = databaseReader.tryCity(ip);

            if (respuestaOpt.isEmpty()) {
                return ubicacionNoDisponible(direccionIp);
            }

            CityResponse respuesta = respuestaOpt.get();

            String ciudad = respuesta.getCity() != null ? respuesta.getCity().getName() : "Desconocida";
            String pais = respuesta.getCountry() != null ? respuesta.getCountry().getName() : "Desconocido";
            String codigoPais = respuesta.getCountry() != null
                    ? respuesta.getCountry().getIsoCode() : "XX";
            Double latitud = respuesta.getLocation() != null
                    ? respuesta.getLocation().getLatitude() : null;
            Double longitud = respuesta.getLocation() != null
                    ? respuesta.getLocation().getLongitude() : null;

            return GeoLocationDTO.builder()
                    .direccionIp(direccionIp)
                    .ciudad(ciudad)
                    .pais(pais)
                    .codigoPais(codigoPais)
                    .latitud(latitud)
                    .longitud(longitud)
                    .ubicacionValida(latitud != null && longitud != null)
                    .build();

        } catch (Exception e) {
            log.warn("No se pudo resolver geolocalizacion para IP {}: {}", direccionIp, e.getMessage());
            return ubicacionNoDisponible(direccionIp);
        }
    }

    @Override
    public boolean estaDisponible() {
        return databaseReader != null;
    }

    private GeoLocationDTO ubicacionNoDisponible(String ip) {
        return GeoLocationDTO.builder()
                .direccionIp(ip)
                .ciudad("Desconocida")
                .pais("Desconocido")
                .codigoPais("XX")
                .ubicacionValida(false)
                .build();
    }

    private boolean esIpPrivada(String ip) {
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.")
                || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || ip.equals("127.0.0.1")
                || ip.equals("0:0:0:0:0:0:0:1");
    }
}
