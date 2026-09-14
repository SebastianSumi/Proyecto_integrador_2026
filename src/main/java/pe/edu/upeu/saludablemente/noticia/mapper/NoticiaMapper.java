package pe.edu.upeu.saludablemente.noticia.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.noticia.dto.NoticiaDto;
import pe.edu.upeu.saludablemente.noticia.entity.NoticiaEntity;

@Component
public class NoticiaMapper {

    public NoticiaDto toDto(NoticiaEntity entity) {
        if (entity == null) return null;
        NoticiaDto dto = new NoticiaDto();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setContenido(entity.getContenido());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setFechaPublicacion(entity.getFechaPublicacion());
        dto.setEstado(entity.getEstado());
        dto.setIdUsuarioAutor(entity.getIdUsuarioAutor());
        return dto;
    }

    public NoticiaEntity toEntity(NoticiaDto dto) {
        if (dto == null) return null;
        NoticiaEntity entity = new NoticiaEntity();
        entity.setId(dto.getId());
        entity.setTitulo(dto.getTitulo());
        entity.setContenido(dto.getContenido());
        entity.setImagenUrl(dto.getImagenUrl());
        entity.setFechaPublicacion(dto.getFechaPublicacion());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : "Borrador");
        entity.setIdUsuarioAutor(dto.getIdUsuarioAutor());
        return entity;
    }
}