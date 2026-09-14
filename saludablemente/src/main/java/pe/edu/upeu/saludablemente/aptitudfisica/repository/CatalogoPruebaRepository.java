package pe.edu.upeu.saludablemente.aptitudfisica.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;

import java.util.List;

public interface CatalogoPruebaRepository extends JpaRepository<CatalogoPrueba, Long> {

    @Query("""
        SELECT c FROM CatalogoPrueba c
        WHERE (:activo IS NULL OR c.activo = :activo)
          AND (:nombre IS NULL OR LOCATE(LOWER(:nombre), LOWER(c.nombrePrueba)) > 0)
        """)
    List<CatalogoPrueba> buscar(@Param("activo") Boolean activo,
                                @Param("nombre") String nombre,
                                Sort sort);
}