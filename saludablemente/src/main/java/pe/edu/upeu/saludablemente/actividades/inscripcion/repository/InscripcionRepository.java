package pe.edu.upeu.saludablemente.actividades.inscripcion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    boolean existsByActividadIdAndPersonaIdAndEstado(
            Long actividadId,
            Long personaId,
            EstadoInscripcion estado
    );
}
