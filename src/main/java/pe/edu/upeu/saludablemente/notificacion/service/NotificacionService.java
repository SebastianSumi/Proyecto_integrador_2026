package pe.edu.upeu.saludablemente.notificacion.service;

import pe.edu.upeu.saludablemente.notificacion.dto.NotificacionDto;
import java.util.List;

public interface NotificacionService {
    List<NotificacionDto> listarPorPersona(Long idPersona);
    List<NotificacionDto> listarPorPersonaYEstadoLectura(Long idPersona, Boolean leido);
    NotificacionDto enviarNotificacion(NotificacionDto dto);
    NotificacionDto marcarComoLeida(Long id);
    void eliminar(Long id);
}