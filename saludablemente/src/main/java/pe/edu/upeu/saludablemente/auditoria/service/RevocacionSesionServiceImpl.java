package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevocacionSesionServiceImpl implements RevocacionSesionService {

    private static final long VIGENCIA_REVOCACION_MINUTOS = 60;

    private final SesionUsuarioService sesionUsuarioService;
    private final Map<String, LocalDateTime> usuariosRevocados = new ConcurrentHashMap<>();

    @Override
    public void revocarSesion(String usuario, String motivo) {
        if (usuario == null) {
            return;
        }
        sesionUsuarioService.invalidarSesiones(usuario);
        usuariosRevocados.put(usuario, LocalDateTime.now());
        log.error("SESION REVOCADA para usuario {} - Motivo: {}", usuario, motivo);
    }

    @Override
    public boolean estaRevocado(String usuario) {
        if (usuario == null || !usuariosRevocados.containsKey(usuario)) {
            return false;
        }
        LocalDateTime momento = usuariosRevocados.get(usuario);
        if (momento.plusMinutes(VIGENCIA_REVOCACION_MINUTOS).isBefore(LocalDateTime.now())) {
            usuariosRevocados.remove(usuario);
            return false;
        }
        return true;
    }
}
