package pe.edu.upeu.saludablemente.noticia.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticiaDto {
    private Long id;
    private String titulo;
    private String contenido;
    private String imagenUrl;
    private LocalDateTime fechaPublicacion;
    private String estado;
    private Long idUsuarioAutor;
}