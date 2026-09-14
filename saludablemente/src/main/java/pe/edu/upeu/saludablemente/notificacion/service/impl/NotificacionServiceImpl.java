package pe.edu.upeu.saludablemente.notificacion.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.notificacion.dto.NotificacionDto;
import pe.edu.upeu.saludablemente.notificacion.entity.NotificacionEntity;
import pe.edu.upeu.saludablemente.notificacion.mapper.NotificacionMapper;
import pe.edu.upeu.saludablemente.notificacion.repository.NotificacionRepository;
import pe.edu.upeu.saludablemente.notificacion.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private NotificacionMapper notificacionMapper;

    @Override
    public List<NotificacionDto> listarPorPersona(Long idPersona) {
        return notificacionRepository.findByIdPersonaOrderByFechaEnvioDesc(idPersona).stream()
                .map(notificacionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificacionDto> listarPorPersonaYEstadoLectura(Long idPersona, Boolean leido) {
        return notificacionRepository.findByIdPersonaAndLeidoOrderByFechaEnvioDesc(idPersona, leido).stream()
                .map(notificacionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificacionDto enviarNotificacion(NotificacionDto dto) {
        NotificacionEntity entity = notificacionMapper.toEntity(dto);
        if (entity.getFechaEnvio() == null) {
            entity.setFechaEnvio(LocalDateTime.now());
        }
        if (entity.getLeido() == null) {
            entity.setLeido(false);
        }
        NotificacionEntity saved = notificacionRepository.save(entity);
        return notificacionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public NotificacionDto marcarComoLeida(Long id) {
        NotificacionEntity existente = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + id));
        
        existente.setLeido(true);
        NotificacionEntity updated = notificacionRepository.save(existente);
        return notificacionMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notificación no encontrada con ID: " + id);
        }
        notificacionRepository.deleteById(id);
    }
}