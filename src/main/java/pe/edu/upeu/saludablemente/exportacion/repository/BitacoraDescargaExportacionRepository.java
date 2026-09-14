package pe.edu.upeu.saludablemente.exportacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.exportacion.entity.BitacoraDescargaExportacionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BitacoraDescargaExportacionRepository
        extends JpaRepository<BitacoraDescargaExportacionEntity, UUID> {

    List<BitacoraDescargaExportacionEntity> findByIdTareaOrderByFechaDescargaDesc(UUID idTarea);

    List<BitacoraDescargaExportacionEntity> findByIdUsuarioDescargaOrderByFechaDescargaDesc(Long idUsuario);

    List<BitacoraDescargaExportacionEntity> findByFechaDescargaBetween(
            LocalDateTime inicio, LocalDateTime fin);

    long countByIdTarea(UUID idTarea);
}
