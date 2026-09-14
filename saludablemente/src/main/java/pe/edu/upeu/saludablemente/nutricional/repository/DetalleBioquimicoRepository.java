package pe.edu.upeu.saludablemente.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleBioquimico;

public interface DetalleBioquimicoRepository extends JpaRepository<DetalleBioquimico, Long> {
}