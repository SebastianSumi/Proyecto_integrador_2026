package pe.edu.upeu.saludablemente.actividades.inscripcion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    Optional<Inscripcion> findByActivityIdAndPersonId(Long activityId, Long personId);
    List<Inscripcion> findAllByActivityIdOrderByEnrolledAtAsc(Long activityId);
    List<Inscripcion> findAllByActivityIdAndStateOrderByEnrolledAtAsc(Long activityId, EstadoInscripcion state);
}
