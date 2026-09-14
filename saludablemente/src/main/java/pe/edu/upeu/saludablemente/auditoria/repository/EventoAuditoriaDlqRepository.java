package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.EventoAuditoriaDlqEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoColaDlq;

import java.util.List;
import java.util.UUID;

public interface EventoAuditoriaDlqRepository
        extends JpaRepository<EventoAuditoriaDlqEntity, UUID> {

    List<EventoAuditoriaDlqEntity> findByEstadoOrderByFechaRegistroAsc(EstadoColaDlq estado);

    long countByEstado(EstadoColaDlq estado);

    @Modifying
    @Transactional
    @Query("UPDATE EventoAuditoriaDlqEntity e SET e.estado = :nuevoEstado, " +
           "e.intentosReintento = e.intentosReintento + 1, " +
           "e.fechaUltimoIntento = CURRENT_TIMESTAMP " +
           "WHERE e.idFallo = :idFallo")
    void actualizarEstadoYReintento(@Param("idFallo") UUID idFallo,
                                    @Param("nuevoEstado") EstadoColaDlq nuevoEstado);
}
