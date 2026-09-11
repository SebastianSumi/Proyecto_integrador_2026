package pe.edu.upeu.saludablemente.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;

import java.util.List;

public interface EvaluacionNutricionalRepository extends JpaRepository<EvaluacionNutricional, Long> {

    List<EvaluacionNutricional> findByPersonaId(Long idPersona);
}