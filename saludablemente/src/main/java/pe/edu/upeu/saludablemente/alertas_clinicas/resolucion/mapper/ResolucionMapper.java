package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.CatalogoAccionCorrectivaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResolucionMapper {

    @Mapping(target = "requiereSeguimiento", source = "entity", qualifiedByName = "mapRequiereSeguimiento")
    CatalogoAccionResponseDTO toAccionDTO(CatalogoAccionCorrectivaEntity entity);

    List<CatalogoAccionResponseDTO> toAccionDTOList(List<CatalogoAccionCorrectivaEntity> entities);

    @Named("mapRequiereSeguimiento")
    default Boolean mapRequiereSeguimiento(CatalogoAccionCorrectivaEntity entity) {
        return entity != null && entity.isRequiereSeguimiento();
    }
}
