package pe.edu.upeu.saludablemente.asistencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.asistencia.entity.AsistenciaEntity;
import java.util.List;
import java.util.Optional;

@Repository 
public interface AsistenciaRepository extends JpaRepository<AsistenciaEntity, Long> {
    List<AsistenciaEntity> findByIdActividad(Long idActividad);
    List<AsistenciaEntity> findByIdPersona(Long idPersona);
    Optional<AsistenciaEntity> findByIdActividadAndIdPersona(Long idActividad, Long idPersona);
}