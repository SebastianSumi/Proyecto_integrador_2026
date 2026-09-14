package pe.edu.upeu.saludablemente.personal.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.personal.entity.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    @Override
    @EntityGraph(attributePaths = "preferenciaComunicacion")
    Optional<Persona> findById(Long id);

    Optional<Persona> findByCelularAndActivoTrue(String celular);

    @EntityGraph(attributePaths = "preferenciaComunicacion")
    @Query("""
        SELECT p FROM Persona p
        WHERE (:activo IS NULL OR p.activo = :activo)
          AND (:nombres IS NULL OR LOCATE(LOWER(:nombres), LOWER(p.nombres)) > 0)
          AND (:celular IS NULL OR p.celular = :celular)
        """)
    List<Persona> buscar(@Param("activo") Boolean activo,
                         @Param("nombres") String nombres,
                         @Param("celular") String celular,
                         Sort sort);
}