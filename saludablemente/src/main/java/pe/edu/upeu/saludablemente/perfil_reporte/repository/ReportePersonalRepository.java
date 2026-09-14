package pe.edu.upeu.saludablemente.perfil_reporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReportePersonalEntity;
import pe.edu.upeu.saludablemente.perfil_reporte.enums.EstadoGeneracionReporte;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportePersonalRepository extends JpaRepository<ReportePersonalEntity, UUID> {

    Optional<ReportePersonalEntity> findFirstByIdPersonaAndPeriodoSemestralOrderByFechaGeneracionDesc(
            Long idPersona, String periodoSemestral);

    List<ReportePersonalEntity> findByIdPersonaOrderByFechaGeneracionDesc(Long idPersona);

    @Query("SELECT r FROM ReportePersonalEntity r WHERE r.idPersona = :idPersona AND r.vigente = true")
    Optional<ReportePersonalEntity> findVigenteByIdPersona(@Param("idPersona") Long idPersona);

    @Modifying
    @Transactional
    @Query("UPDATE ReportePersonalEntity r SET r.vigente = false WHERE r.idPersona = :idPersona AND r.vigente = true")
    void desactivarReportesVigentes(@Param("idPersona") Long idPersona);

    long countByEstadoGeneracion(EstadoGeneracionReporte estado);

    @Query("SELECT r FROM ReportePersonalEntity r WHERE r.estadoGeneracion = :estado AND r.fechaGeneracion < :fechaLimite")
    List<ReportePersonalEntity> findByEstadoGeneracionAndFechaGeneracionBefore(
            @Param("estado") EstadoGeneracionReporte estado,
            @Param("fechaLimite") LocalDateTime fechaLimite);

    boolean existsByIdPersonaAndPeriodoSemestral(Long idPersona, String periodoSemestral);

    @Query("SELECT COALESCE(MAX(r.versionReporte), 0) FROM ReportePersonalEntity r " +
           "WHERE r.idPersona = :idPersona AND r.periodoSemestral = :periodo")
    Integer findMaxVersionByPersonaAndPeriodo(@Param("idPersona") Long idPersona,
                                              @Param("periodo") String periodo);
}
