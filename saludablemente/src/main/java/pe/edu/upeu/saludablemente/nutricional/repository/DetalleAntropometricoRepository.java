package pe.edu.upeu.saludablemente.nutricional.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleAntropometrico;

public interface DetalleAntropometricoRepository extends JpaRepository<DetalleAntropometrico, Long> {
}