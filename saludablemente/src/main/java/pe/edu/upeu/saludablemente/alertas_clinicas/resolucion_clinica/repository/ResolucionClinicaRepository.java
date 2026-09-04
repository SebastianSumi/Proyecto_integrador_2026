package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResolucionClinicaRepository extends JpaRepository<ResolucionClinicaEntity, UUID> {

    Optional<ResolucionClinicaEntity> findByAlerta_IdAlerta(UUID idAlerta);

    boolean existsByAlerta_IdAlerta(UUID idAlerta);

    List<ResolucionClinicaEntity> findByIdUsuarioEvaluador(Long idEvaluador);

    @Query("SELECT r FROM ResolucionClinicaEntity r WHERE r.fechaAtencion BETWEEN :inicio AND :fin")
    List<ResolucionClinicaEntity> findByFechaAtencionBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM ResolucionClinicaEntity r WHERE r.idUsuarioEvaluador = :idEvaluador")
    long countByEvaluador(@Param("idEvaluador") Long idEvaluador);

    @Query("SELECT r FROM ResolucionClinicaEntity r WHERE r.esSeguimientoProgramado = true AND r.fechaSeguimientoProgramado <= :fecha")
    List<ResolucionClinicaEntity> findSeguimientosPendientes(@Param("fecha") LocalDateTime fecha);
}