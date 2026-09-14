package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.ParticionBitacoraEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticionBitacoraRepository
        extends JpaRepository<ParticionBitacoraEntity, UUID> {

    List<ParticionBitacoraEntity> findByTablaOrigenOrderByRangoFechaInicioAsc(String tablaOrigen);

    Optional<ParticionBitacoraEntity> findByNombreParticion(String nombreParticion);

    @Query("SELECT p FROM ParticionBitacoraEntity p " +
           "WHERE p.tablaOrigen = :tabla AND p.archivada = 'N' AND p.rangoFechaFin < :fechaLimite")
    List<ParticionBitacoraEntity> findCandidatasArchivado(
            @Param("tabla") String tabla,
            @Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT p FROM ParticionBitacoraEntity p " +
           "WHERE p.tablaOrigen = :tabla AND p.purgada = 'N' AND p.rangoFechaFin < :fechaLimite")
    List<ParticionBitacoraEntity> findCandidatasPurga(
            @Param("tabla") String tabla,
            @Param("fechaLimite") LocalDateTime fechaLimite);

    @Modifying
    @Transactional
    @Query("UPDATE ParticionBitacoraEntity p SET p.archivada = 'S', " +
           "p.idManifiesto = :idManifiesto, p.estado = 'ARCHIVADA' " +
           "WHERE p.idParticion = :idParticion")
    void marcarComoArchivada(@Param("idParticion") UUID idParticion,
                             @Param("idManifiesto") UUID idManifiesto);

    @Modifying
    @Transactional
    @Query("UPDATE ParticionBitacoraEntity p SET p.purgada = 'S', p.estado = 'PURGADA' " +
           "WHERE p.idParticion = :idParticion")
    void marcarComoPurgada(@Param("idParticion") UUID idParticion);
}
