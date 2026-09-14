package pe.edu.upeu.saludablemente.notificacion.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.notificacion.dto.NotificacionDto;
import pe.edu.upeu.saludablemente.notificacion.entity.NotificacionEntity;

@Component
public class NotificacionMapper {

    public NotificacionDto toDto(NotificacionEntity entity) {
        if (entity == null) return null;
        NotificacionDto dto = new NotificacionDto();
        dto.setId(entity.getId());
        dto.setIdPersona(entity.getIdPersona());
        dto.setTitulo(entity.getTitulo());
        dto.setMensaje(entity.getMensaje());
        dto.setModuloOrigen(entity.getModuloOrigen());
        dto.setReferenciaId(entity.getReferenciaId());
        dto.setLeido(entity.getLeido());
        dto.setFechaEnvio(entity.getFechaEnvio());
        return dto;
    }

    public NotificacionEntity toEntity(NotificacionDto dto) {
        if (dto == null) return null;
        NotificacionEntity entity = new NotificacionEntity();
        entity.setId(dto.getId());
        entity.setIdPersona(dto.getIdPersona());
        entity.setTitulo(dto.getTitulo());
        entity.setMensaje(dto.getMensaje());
        entity.setModuloOrigen(dto.getModuloOrigen());
        entity.setReferenciaId(dto.getReferenciaId());
        entity.setLeido(dto.getLeido() != null ? dto.getLeido() : false);
        entity.setFechaEnvio(dto.getFechaEnvio());
        return entity;
    }
}