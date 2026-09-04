package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaDetalleEntity;

import java.util.List;
import java.util.UUID;

public interface AlertaClinicaDetalleRepository extends JpaRepository<AlertaClinicaDetalleEntity, UUID> {
    List<AlertaClinicaDetalleEntity> findByAlerta_IdAlerta(UUID idAlerta);
    void deleteByAlerta_IdAlerta(UUID idAlerta);
}