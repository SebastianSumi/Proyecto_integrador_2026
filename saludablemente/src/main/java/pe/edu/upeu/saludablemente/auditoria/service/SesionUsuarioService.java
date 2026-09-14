package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.LoginAttemptDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.SesionUsuarioEntity;

import java.util.List;
import java.util.Optional;

public interface SesionUsuarioService {

    void registrarLogin(LoginAttemptDTO loginAttempt);

    Optional<SesionUsuarioEntity> obtenerUltimaSesion(String usuario);

    List<SesionUsuarioEntity> obtenerHistorialSesiones(String usuario);

    void invalidarSesiones(String usuario);
}
