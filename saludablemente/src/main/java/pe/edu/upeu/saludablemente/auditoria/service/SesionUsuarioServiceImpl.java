package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.GeoLocationDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.LoginAttemptDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.SesionUsuarioEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.SesionUsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesionUsuarioServiceImpl implements SesionUsuarioService {

    private final SesionUsuarioRepository sesionRepository;
    private final GeoIpService geoIpService;

    @Override
    @Transactional
    public void registrarLogin(LoginAttemptDTO loginAttempt) {
        if (loginAttempt == null || !loginAttempt.isExitoso()) {
            return;
        }

        GeoLocationDTO ubicacion = geoIpService.resolverUbicacion(loginAttempt.getDireccionIp());

        SesionUsuarioEntity sesion = SesionUsuarioEntity.builder()
                .usuario(loginAttempt.getUsuario())
                .direccionIp(loginAttempt.getDireccionIp())
                .ciudad(ubicacion.getCiudad())
                .pais(ubicacion.getPais())
                .codigoPais(ubicacion.getCodigoPais())
                .latitud(ubicacion.getLatitud())
                .longitud(ubicacion.getLongitud())
                .userAgent(loginAttempt.getUserAgent())
                .fechaLogin(loginAttempt.getTimestamp() != null
                        ? loginAttempt.getTimestamp() : LocalDateTime.now())
                .sesionActiva("S")
                .build();

        sesionRepository.save(sesion);
        log.debug("Sesion registrada para usuario {} desde {}",
                loginAttempt.getUsuario(), ubicacion.getCiudad());
    }

    @Override
    public Optional<SesionUsuarioEntity> obtenerUltimaSesion(String usuario) {
        return sesionRepository.findUltimaSesion(usuario);
    }

    @Override
    public List<SesionUsuarioEntity> obtenerHistorialSesiones(String usuario) {
        return sesionRepository.findByUsuarioOrderByFechaLoginDesc(usuario);
    }

    @Override
    @Transactional
    public void invalidarSesiones(String usuario) {
        sesionRepository.invalidarSesionesDeUsuario(usuario);
        log.info("Sesiones invalidadas para usuario {}", usuario);
    }
}
