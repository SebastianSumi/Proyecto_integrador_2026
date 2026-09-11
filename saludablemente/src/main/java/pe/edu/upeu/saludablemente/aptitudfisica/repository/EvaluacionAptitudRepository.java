package pe.edu.upeu.saludablemente.aptitudfisica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.EvaluacionAptitud;

import java.util.List;

public interface EvaluacionAptitudRepository extends JpaRepository<EvaluacionAptitud, Long> {

    List<EvaluacionAptitud> findByPersonaId(Long idPersona);
}