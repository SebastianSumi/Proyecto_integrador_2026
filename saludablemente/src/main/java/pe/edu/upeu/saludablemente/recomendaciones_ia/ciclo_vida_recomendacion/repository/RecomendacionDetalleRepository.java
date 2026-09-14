package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionDetalleEntity;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.TipoSeccionRecomendacion;

import java.util.List;
import java.util.UUID;

public interface RecomendacionDetalleRepository extends JpaRepository<RecomendacionDetalleEntity, UUID> {

    List<RecomendacionDetalleEntity> findByRecomendacion_IdRecomendacion(UUID idRecomendacion);

    List<RecomendacionDetalleEntity> findByRecomendacion_IdRecomendacionAndTipoSeccion(
            UUID idRecomendacion,
            TipoSeccionRecomendacion tipoSeccion
    );
}