package pe.edu.upeu.saludablemente.noticia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "NOTICIAS", schema = "SALUD_PERSONAL")
public class NoticiaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTICIA")
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "IMAGEN_URL", length = 250)
    private String imagenUrl;

    @Column(name = "FECHA_PUBLICACION")
    private LocalDateTime fechaPublicacion;

    @Column(nullable = false, length = 20)
    private String estado; // Borrador, Publicado, Archivado

    @Column(name = "ID_USUARIO_AUTOR", nullable = false)
    private Long idUsuarioAutor;
}