package pe.edu.upeu.saludablemente.notificacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.notificacion.entity.NotificacionEntity;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {
    List<NotificacionEntity> findByIdPersonaOrderByFechaEnvioDesc(Long idPersona);
    List<NotificacionEntity> findByIdPersonaAndLeidoOrderByFechaEnvioDesc(Long idPersona, Boolean leido);
}