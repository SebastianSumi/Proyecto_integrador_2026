package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaAgregadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaClinicaRepository extends JpaRepository<AlertaClinicaEntity, UUID>,
        JpaSpecificationExecutor<AlertaClinicaEntity> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<AlertaClinicaEntity> findById(UUID id);

    @EntityGraph(attributePaths = "detalles")
    @Query("SELECT DISTINCT a FROM AlertaClinicaEntity a " +
            "LEFT JOIN a.detalles d " +
            "WHERE (:estado IS NULL OR a.estado = :estado) " +
            "AND (:severidad IS NULL OR a.nivelSeveridad = :severidad) " +
            "AND (:idPersona IS NULL OR a.idPersona = :idPersona) " +
            "AND (:tipoIndicador IS NULL OR d.tipoIndicador = :tipoIndicador)")
    Page<AlertaClinicaEntity> buscarConFiltros(
            @Param("estado") EstadoAlerta estado,
            @Param("severidad") NivelSeveridad severidad,
            @Param("idPersona") Long idPersona,
            @Param("tipoIndicador") TipoIndicador tipoIndicador,
            Pageable pageable
    );

    @Query("SELECT a FROM AlertaClinicaEntity a " +
            "WHERE a.idPersona = :idPersona " +
            "AND a.estado IN :estados " +
            "AND EXISTS (SELECT d FROM AlertaClinicaDetalleEntity d WHERE d.alerta = a AND d.tipoIndicador = :tipoIndicador) " +
            "AND a.fechaGeneracion > :fechaLimite")
    List<AlertaClinicaEntity> findAlertasDuplicadasPendientes(
            @Param("idPersona") Long idPersona,
            @Param("tipoIndicador") TipoIndicador tipoIndicador,
            @Param("estados") List<EstadoAlerta> estados,
            @Param("fechaLimite") LocalDateTime fechaLimite
    );

    @Query("SELECT a FROM AlertaClinicaEntity a " +
            "WHERE a.estado IN :estados " +
            "AND a.fechaVencimientoSla < :fechaActual")
    List<AlertaClinicaEntity> findAlertasVencidas(
            @Param("estados") List<EstadoAlerta> estados,
            @Param("fechaActual") LocalDateTime fechaActual
    );

    @EntityGraph(attributePaths = "detalles")
    List<AlertaClinicaEntity> findByIdPersonaOrderByFechaGeneracionDesc(Long idPersona);

    @Query("SELECT new pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaAgregadoDTO(" +
            "COUNT(a), " +
            "SUM(CASE WHEN a.nivelSeveridad = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad.CRITICO THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN a.estado = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta.PENDIENTE THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN a.estado = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta.EN_REVISION THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN a.estado = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta.ATENDIDA THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN a.estado = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta.DESESTIMADA THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN a.estado = pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta.VENCIDO THEN 1L ELSE 0L END), " +
            "COALESCE(AVG(a.scoreRiesgo), 0.0)) " +
            "FROM AlertaClinicaEntity a")
    AlertaAgregadoDTO obtenerMetricasAgregadas();

    @Modifying
    @Transactional
    @Query("UPDATE AlertaClinicaEntity a SET a.estado = :nuevoEstado, a.version = a.version + 1 " +
            "WHERE a.idAlerta = :idAlerta AND a.version = :versionEsperada")
    int actualizarEstadoConVersion(
            @Param("idAlerta") UUID idAlerta,
            @Param("nuevoEstado") EstadoAlerta nuevoEstado,
            @Param("versionEsperada") Long versionEsperada
    );

    List<AlertaClinicaEntity> findByEstado(EstadoAlerta estado);

    long countByEstado(EstadoAlerta estado);
}
