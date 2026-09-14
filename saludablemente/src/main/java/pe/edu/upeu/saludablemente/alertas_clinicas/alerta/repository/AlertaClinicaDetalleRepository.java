package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaDetalleEntity;

import java.util.UUID;

@Repository
public interface AlertaClinicaDetalleRepository extends JpaRepository<AlertaClinicaDetalleEntity, UUID> {
}
