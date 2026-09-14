package pe.edu.upeu.saludablemente.perfil_reporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ColaGeneracionReportesEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaGeneracionReportesRepository
        extends JpaRepository<ColaGeneracionReportesEntity, UUID> {

    List<ColaGeneracionReportesEntity> findByEstadoColaOrderByFechaSolicitudAsc(String estadoCola);

    Optional<ColaGeneracionReportesEntity> findFirstByIdPersonaAndPeriodoSemestralOrderByFechaSolicitudDesc(
            Long idPersona, String periodoSemestral);

    @Modifying
    @Transactional
    @Query("UPDATE ColaGeneracionReportesEntity c SET c.estadoCola = :nuevoEstado, " +
           "c.intentosAcumulados = c.intentosAcumulados + 1, " +
           "c.ultimoError = :error, " +
           "c.fechaUltimaActualizacion = CURRENT_TIMESTAMP " +
           "WHERE c.idTarea = :idTarea")
    void actualizarEstadoYReintentos(@Param("idTarea") UUID idTarea,
                                     @Param("nuevoEstado") String nuevoEstado,
                                     @Param("error") String error);

    @Modifying
    @Transactional
    @Query("DELETE FROM ColaGeneracionReportesEntity c " +
           "WHERE c.estadoCola = 'COMPLETADO' " +
           "AND c.fechaUltimaActualizacion < :fechaLimite")
    void limpiarTareasCompletadasAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
}
