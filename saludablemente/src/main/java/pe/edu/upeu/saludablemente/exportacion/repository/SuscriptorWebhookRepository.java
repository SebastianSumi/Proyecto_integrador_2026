package pe.edu.upeu.saludablemente.exportacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.exportacion.entity.SuscriptorWebhookEntity;

import java.util.List;
import java.util.UUID;

public interface SuscriptorWebhookRepository extends JpaRepository<SuscriptorWebhookEntity, UUID> {

    List<SuscriptorWebhookEntity> findByActivo(String activo);

    List<SuscriptorWebhookEntity> findByActivoOrderByFechaRegistroDesc(String activo);
}
