package pe.edu.upeu.saludablemente.metas.meta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByPersonaId(Long personaId);
}
