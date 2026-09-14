package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoAlertaSeguridad;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertaSeguridadRepository
        extends JpaRepository<AlertaSeguridadEntity, UUID> {

    List<AlertaSeguridadEntity> findByEstadoAlertaOrderByFechaDeteccionDesc(EstadoAlertaSeguridad estado);

    List<AlertaSeguridadEntity> findByTipoAnomaliaOrderByFechaDeteccionDesc(TipoAnomalia tipo);

    List<AlertaSeguridadEntity> findByUsuarioAfectadoOrderByFechaDeteccionDesc(String usuario);

    List<AlertaSeguridadEntity> findByFechaDeteccionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(a) FROM AlertaSeguridadEntity a " +
           "WHERE a.usuarioAfectado = :usuario AND a.fechaDeteccion > :desde")
    long countByUsuarioAfectadoAndFechaDeteccionAfter(
            @Param("usuario") String usuario,
            @Param("desde") LocalDateTime desde);

    @Modifying
    @Transactional
    @Query("UPDATE AlertaSeguridadEntity a SET a.estadoAlerta = :estado, " +
           "a.fechaResolucion = CURRENT_TIMESTAMP, " +
           "a.resueltoPor = :resueltoPor, " +
           "a.observaciones = :observaciones " +
           "WHERE a.idAlerta = :idAlerta")
    void resolverAlerta(@Param("idAlerta") UUID idAlerta,
                        @Param("estado") EstadoAlertaSeguridad estado,
                        @Param("resueltoPor") String resueltoPor,
                        @Param("observaciones") String observaciones);
}
