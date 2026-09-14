package pe.edu.upeu.saludablemente.noticia.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.noticia.dto.NoticiaDto;
import pe.edu.upeu.saludablemente.noticia.entity.NoticiaEntity;
import pe.edu.upeu.saludablemente.noticia.mapper.NoticiaMapper;
import pe.edu.upeu.saludablemente.noticia.repository.NoticiaRepository;
import pe.edu.upeu.saludablemente.noticia.service.NoticiaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticiaServiceImpl implements NoticiaService {

    @Autowired
    private NoticiaRepository noticiaRepository;

    @Autowired
    private NoticiaMapper noticiaMapper;

    @Override
    public List<NoticiaDto> listarTodas() {
        return noticiaRepository.findAll().stream()
                .map(noticiaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoticiaDto> listarPublicadas() {
        return noticiaRepository.findByEstado("Publicado").stream()
                .map(noticiaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public NoticiaDto obtenerPorId(Long id) {
        NoticiaEntity entity = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));
        return noticiaMapper.toDto(entity);
    }

    @Override
    @Transactional
    public NoticiaDto crearNoticia(NoticiaDto dto) {
        NoticiaEntity entity = noticiaMapper.toEntity(dto);
        if (entity.getFechaPublicacion() == null && "Publicado".equalsIgnoreCase(entity.getEstado())) {
            entity.setFechaPublicacion(LocalDateTime.now());
        }
        NoticiaEntity saved = noticiaRepository.save(entity);
        return noticiaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public NoticiaDto actualizarNoticia(Long id, NoticiaDto dto) {
        NoticiaEntity existente = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));

        existente.setTitulo(dto.getTitulo());
        existente.setContenido(dto.getContenido());
        existente.setImagenUrl(dto.getImagenUrl());
        
        if ("Publicado".equalsIgnoreCase(dto.getEstado()) && !"Publicado".equalsIgnoreCase(existente.getEstado())) {
            existente.setFechaPublicacion(LocalDateTime.now());
        }
        existente.setEstado(dto.getEstado());
        existente.setIdUsuarioAutor(dto.getIdUsuarioAutor());

        NoticiaEntity updated = noticiaRepository.save(existente);
        return noticiaMapper.toDto(updated);
    }

    @Override
    @Transactional
    public NoticiaDto despublicarNoticia(Long id) {
        NoticiaEntity existente = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));
        
        existente.setEstado("Archivado");
        NoticiaEntity updated = noticiaRepository.save(existente);
        return noticiaMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!noticiaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Noticia no encontrada con ID: " + id);
        }
        noticiaRepository.deleteById(id);
    }
}