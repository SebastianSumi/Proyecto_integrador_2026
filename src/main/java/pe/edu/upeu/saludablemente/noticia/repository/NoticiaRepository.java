package pe.edu.upeu.saludablemente.noticia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.noticia.entity.NoticiaEntity;
import java.util.List;

@Repository
public interface NoticiaRepository extends JpaRepository<NoticiaEntity, Long> {
    List<NoticiaEntity> findByEstado(String estado);
}