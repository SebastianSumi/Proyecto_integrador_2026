package pe.edu.upeu.saludablemente.noticia.service;

import pe.edu.upeu.saludablemente.noticia.dto.NoticiaDto;
import java.util.List;

public interface NoticiaService {
    List<NoticiaDto> listarTodas();
    List<NoticiaDto> listarPublicadas();
    NoticiaDto obtenerPorId(Long id);
    NoticiaDto crearNoticia(NoticiaDto dto);
    NoticiaDto actualizarNoticia(Long id, NoticiaDto dto);
    NoticiaDto despublicarNoticia(Long id);
    void eliminar(Long id);
}