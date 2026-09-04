package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertaClinicaRepository extends JpaRepository<AlertaClinicaEntity, UUID>,
        JpaSpecificationExecutor<AlertaClinicaEntity> {

    /**
     * Buscar alertas pendientes duplicadas para el mismo paciente, tipo y día
     */
    @Query("SELECT a FROM AlertaClinicaEntity a " +
            "WHERE a.persona.idPersona = :personaId " +
            "AND a.estado IN :estados " +
            "AND EXISTS (SELECT d FROM AlertaClinicaDetalleEntity d " +
            "           WHERE d.alerta = a AND d.tipoIndicador = :tipoIndicador) " +
            "AND a.fechaGeneracion > :fechaLimite")
    List<AlertaClinicaEntity> findAlertasDuplicadasPendientes(
            @Param("personaId") Long personaId,
            @Param("tipoIndicador") String tipoIndicador,
            @Param("estados") List<EstadoAlerta> estados,
            @Param("fechaLimite") LocalDateTime fechaLimite
    );

    /**
     * Buscar alertas vencidas
     */
    @Query("SELECT a FROM AlertaClinicaEntity a " +
            "WHERE a.estado IN :estados " +
            "AND a.fechaVencimientoSla < :fechaActual")
    List<AlertaClinicaEntity> findAlertasVencidas(
            @Param("estados") List<EstadoAlerta> estados,
            @Param("fechaActual") LocalDateTime fechaActual
    );

    /**
     * Actualizar estado de alerta
     */
    @Modifying
    @Transactional
    @Query("UPDATE AlertaClinicaEntity a SET a.estado = :nuevoEstado, a.version = a.version + 1 " +
            "WHERE a.idAlerta = :idAlerta AND a.version = :versionEsperada")
    int actualizarEstadoConVersion(
            @Param("idAlerta") UUID idAlerta,
            @Param("nuevoEstado") EstadoAlerta nuevoEstado,
            @Param("versionEsperada") Long versionEsperada
    );

    /**
     * Contar alertas pendientes por evaluador (para balanceo de carga)
     */
    @Query("SELECT COUNT(a) FROM AlertaClinicaEntity a " +
            "JOIN a.resolucion r " +
            "WHERE r.idUsuarioEvaluador = :idEvaluador " +
            "AND a.estado IN :estados")
    long countAlertasPendientesByEvaluador(
            @Param("idEvaluador") Long idEvaluador,
            @Param("estados") List<EstadoAlerta> estados
    );

    Optional<AlertaClinicaEntity> findByIdAlertaAndPersona_IdPersona(UUID idAlerta, Long idPersona);
}