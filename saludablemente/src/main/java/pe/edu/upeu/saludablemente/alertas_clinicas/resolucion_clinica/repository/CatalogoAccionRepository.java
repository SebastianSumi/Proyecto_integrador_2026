package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.CatalogoAccionCorrectivaEntity;

import java.util.Optional;

public interface CatalogoAccionRepository extends JpaRepository<CatalogoAccionCorrectivaEntity, Long> {
    Optional<CatalogoAccionCorrectivaEntity> findByCodigoTipificado(String codigoTipificado);
    boolean existsByCodigoTipificado(String codigoTipificado);
}