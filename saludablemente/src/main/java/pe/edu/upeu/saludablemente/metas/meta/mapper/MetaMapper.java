package pe.edu.upeu.saludablemente.metas.meta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;

@Mapper(componentModel = "spring")
public interface MetaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Meta toEntity(MetaCreateRequest request);

    MetaResponse toResponse(Meta meta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "personaId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    void updateEntity(MetaUpdateRequest request, @MappingTarget Meta meta);
}
