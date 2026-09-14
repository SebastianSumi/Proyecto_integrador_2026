package pe.edu.upeu.saludablemente.metas.meta.service;

import java.util.List;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;

public interface MetaService {

    List<MetaResponse> findAll();

    List<MetaResponse> findByPersonaId(Long personaId);

    MetaResponse findById(Long id);

    MetaResponse create(MetaCreateRequest request);

    MetaResponse update(Long id, MetaUpdateRequest request);

    MetaResponse complete(Long id);

    void delete(Long id);
}
