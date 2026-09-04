package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;

import java.util.Optional;
import java.util.UUID;

public interface ResolucionClinicaRepository extends JpaRepository<ResolucionClinicaEntity, UUID> {
    Optional<ResolucionClinicaEntity> findByAlerta_IdAlerta(UUID idAlerta);
    boolean existsByAlerta_IdAlerta(UUID idAlerta);
}