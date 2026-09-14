package pe.edu.upeu.saludablemente.asistencia.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.asistencia.dto.AsistenciaDto;
import pe.edu.upeu.saludablemente.asistencia.entity.AsistenciaEntity;

@Component
public class AsistenciaMapper {

    public AsistenciaDto toDto(AsistenciaEntity entity) {
        if (entity == null) return null;
        AsistenciaDto dto = new AsistenciaDto();
        dto.setId(entity.getId());
        dto.setIdActividad(entity.getIdActividad());
        dto.setIdPersona(entity.getIdPersona());
        dto.setFechaHoraMarcado(entity.getFechaHoraMarcado());
        dto.setMetodo(entity.getMetodo());
        dto.setEstado(entity.getEstado());
        dto.setEsOffline(entity.getEsOffline());
        dto.setDispositivoUuid(entity.getDispositivoUuid());
        return dto;
    }

    public AsistenciaEntity toEntity(AsistenciaDto dto) {
        if (dto == null) return null;
        AsistenciaEntity entity = new AsistenciaEntity();
        entity.setId(dto.getId());
        entity.setIdActividad(dto.getIdActividad());
        entity.setIdPersona(dto.getIdPersona());
        entity.setFechaHoraMarcado(dto.getFechaHoraMarcado());
        entity.setMetodo(dto.getMetodo());
        entity.setEstado(dto.getEstado());
        entity.setEsOffline(dto.getEsOffline() != null ? dto.getEsOffline() : false);
        entity.setDispositivoUuid(dto.getDispositivoUuid());
        return entity;
    }
}