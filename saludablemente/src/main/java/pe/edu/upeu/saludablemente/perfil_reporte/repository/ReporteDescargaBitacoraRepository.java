package pe.edu.upeu.saludablemente.perfil_reporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReporteDescargaBitacoraEntity;

import java.util.List;
import java.util.UUID;

public interface ReporteDescargaBitacoraRepository
        extends JpaRepository<ReporteDescargaBitacoraEntity, UUID> {

    List<ReporteDescargaBitacoraEntity> findByIdReporteOrderByFechaDescargaDesc(UUID idReporte);

    long countByIdReporte(UUID idReporte);
}
