package pe.edu.upeu.saludablemente.aptitudfisica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;

import java.util.List;

public interface CatalogoPruebaRepository extends JpaRepository<CatalogoPrueba, Long> {

    List<CatalogoPrueba> findByActivoTrue();
}