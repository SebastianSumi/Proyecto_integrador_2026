package pe.edu.upeu.saludablemente.exportacion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.exportacion.entity.TareaExportacionEntity;
import pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TareaExportacionRepository extends JpaRepository<TareaExportacionEntity, UUID> {

    Optional<TareaExportacionEntity> findByHashSha256(String hashSha256);

    List<TareaExportacionEntity> findByEstadoTareaOrderByFechaSolicitudAsc(EstadoTarea estadoTarea);

    List<TareaExportacionEntity> findByIdUsuarioSolicitanteOrderByFechaSolicitudDesc(Long idUsuario);

    @Query("SELECT t FROM TareaExportacionEntity t " +
           "WHERE t.fechaSolicitud >= :desde " +
           "ORDER BY t.fechaSolicitud DESC")
    Page<TareaExportacionEntity> findUltimas(@Param("desde") LocalDateTime desde, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TareaExportacionEntity t " +
           "WHERE t.idUsuarioSolicitante = :idUsuario " +
           "AND t.fechaSolicitud > :desde")
    long contarPorUsuarioDesde(@Param("idUsuario") Long idUsuario,
                                @Param("desde") LocalDateTime desde);

    @Query("SELECT COALESCE(SUM(t.totalRegistrosProcesados), 0) " +
           "FROM TareaExportacionEntity t " +
           "WHERE t.idUsuarioSolicitante = :idUsuario " +
           "AND t.fechaSolicitud > :desde " +
           "AND t.estadoTarea = pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea.COMPLETADO")
    long sumarRegistrosExportadosPorUsuarioDesde(@Param("idUsuario") Long idUsuario,
                                                  @Param("desde") LocalDateTime desde);

    @Query("SELECT t FROM TareaExportacionEntity t " +
           "WHERE t.estadoTarea = pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea.COMPLETADO " +
           "AND t.fechaFinalizacion < :fechaLimite " +
           "AND t.rutaAlmacenamiento IS NOT NULL")
    List<TareaExportacionEntity> findCompletadasAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
}
