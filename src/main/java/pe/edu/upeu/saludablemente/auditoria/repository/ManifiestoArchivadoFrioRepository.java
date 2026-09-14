package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.ManifiestoArchivadoFrioEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoManifiesto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManifiestoArchivadoFrioRepository
        extends JpaRepository<ManifiestoArchivadoFrioEntity, UUID> {

    List<ManifiestoArchivadoFrioEntity> findByEstadoOrderByFechaExportacionAsc(EstadoManifiesto estado);

    Optional<ManifiestoArchivadoFrioEntity>
            findFirstByRangoFechaInicioLessThanEqualAndRangoFechaFinGreaterThanEqual(
                    LocalDateTime fechaFin, LocalDateTime fechaInicio);

    @Query("SELECT m FROM ManifiestoArchivadoFrioEntity m " +
           "WHERE m.estado = :estado AND m.fechaExportacion < :fechaLimite")
    List<ManifiestoArchivadoFrioEntity> findCandidatosPurga(
            @Param("estado") EstadoManifiesto estado,
            @Param("fechaLimite") LocalDateTime fechaLimite);

    @Modifying
    @Transactional
    @Query("UPDATE ManifiestoArchivadoFrioEntity m SET m.estado = :nuevoEstado, " +
           "m.fechaPurga = CURRENT_TIMESTAMP WHERE m.idManifiesto = :idManifiesto")
    void marcarComoPurgado(@Param("idManifiesto") UUID idManifiesto,
                           @Param("nuevoEstado") EstadoManifiesto nuevoEstado);
}
