package pe.edu.upeu.saludablemente.personal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.personal.entity.PreferenciaComunicacion;

import java.util.Optional;

public interface PreferenciaComunicacionRepository extends JpaRepository<PreferenciaComunicacion, Long> {

    Optional<PreferenciaComunicacion> findByPersonaId(Long idPersona);
}