package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.auditoria.entity.PoliticaRetencionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PoliticaRetencionRepository
        extends JpaRepository<PoliticaRetencionEntity, UUID> {

    Optional<PoliticaRetencionEntity> findByTipoDato(String tipoDato);

    List<PoliticaRetencionEntity> findByActiva(String activa);
}
