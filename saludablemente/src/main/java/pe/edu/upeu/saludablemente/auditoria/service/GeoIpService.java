package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.GeoLocationDTO;

public interface GeoIpService {

    GeoLocationDTO resolverUbicacion(String direccionIp);

    boolean estaDisponible();
}
