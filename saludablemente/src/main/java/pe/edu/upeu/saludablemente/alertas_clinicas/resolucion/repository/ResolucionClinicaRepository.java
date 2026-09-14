package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResolucionClinicaRepository extends JpaRepository<ResolucionClinicaEntity, UUID> {

    boolean existsByAlerta_IdAlerta(UUID idAlerta);

    Optional<ResolucionClinicaEntity> findByAlerta_IdAlerta(UUID idAlerta);

    List<ResolucionClinicaEntity> findByIdUsuarioEvaluador(Long idUsuarioEvaluador);
}
