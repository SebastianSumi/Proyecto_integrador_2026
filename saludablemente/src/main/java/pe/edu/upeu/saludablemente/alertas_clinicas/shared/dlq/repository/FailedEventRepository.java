package pe.edu.upeu.saludablemente.alertas_clinicas.shared.dlq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.dlq.entity.FailedEventEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoDlq;

import java.util.List;
import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEventEntity, UUID> {

    List<FailedEventEntity> findByEstadoOrderByFechaFalloAsc(EstadoDlq estado);

    @Modifying
    @Transactional
    @Query("UPDATE FailedEventEntity f SET f.estado = :estado, f.contadorReintentos = f.contadorReintentos + 1, " +
            "f.ultimoIntento = CURRENT_TIMESTAMP WHERE f.idFallo = :idFallo")
    void actualizarEstadoYReintentos(@Param("idFallo") UUID idFallo, @Param("estado") EstadoDlq estado);

    @Modifying
    @Transactional
    @Query("UPDATE FailedEventEntity f SET f.estado = :estado WHERE f.idFallo = :idFallo")
    void actualizarEstado(@Param("idFallo") UUID idFallo, @Param("estado") EstadoDlq estado);
}