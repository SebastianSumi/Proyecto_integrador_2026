package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.CatalogoAccionCorrectivaEntity;

import java.util.Optional;

@Repository
public interface CatalogoAccionRepository extends JpaRepository<CatalogoAccionCorrectivaEntity, Long> {

    Optional<CatalogoAccionCorrectivaEntity> findByCodigoTipificado(String codigoTipificado);
}
