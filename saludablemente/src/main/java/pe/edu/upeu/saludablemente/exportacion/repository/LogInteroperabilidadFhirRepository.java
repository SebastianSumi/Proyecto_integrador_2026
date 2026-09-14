package pe.edu.upeu.saludablemente.exportacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.exportacion.entity.LogInteroperabilidadFhirEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LogInteroperabilidadFhirRepository
        extends JpaRepository<LogInteroperabilidadFhirEntity, UUID> {

    List<LogInteroperabilidadFhirEntity> findByIdSistemaClienteOrderByFechaPeticionDesc(String idSistemaCliente);

    List<LogInteroperabilidadFhirEntity> findByRecursoSolicitadoOrderByFechaPeticionDesc(String recursoSolicitado);

    List<LogInteroperabilidadFhirEntity> findByFechaPeticionBetween(LocalDateTime inicio, LocalDateTime fin);

    long countByCodigoHttpRespuesta(Integer codigoHttpRespuesta);
}
