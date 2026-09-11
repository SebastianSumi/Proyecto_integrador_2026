package pe.edu.upeu.saludablemente.aptitudfisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.DetallePruebaFisicaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.DetallePruebaFisica;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.EvaluacionAptitud;

@Mapper(componentModel = "spring")
public interface AptitudFisicaMapper {

    CatalogoPruebaDto toCatalogoPruebaDto(CatalogoPrueba catalogoPrueba);

    @Mapping(target = "detalles", ignore = true)
    EvaluacionAptitud toEntity(EvaluacionAptitudRequestDto request);

    @Mapping(target = "idPrueba", source = "catalogoPrueba.idPrueba")
    @Mapping(target = "nombrePrueba", source = "catalogoPrueba.nombrePrueba")
    DetallePruebaFisicaDto toDetalleDto(DetallePruebaFisica detalle);

    EvaluacionAptitudResponseDto toResponse(EvaluacionAptitud evaluacion);

    default DetallePruebaFisica toDetalle(DetallePruebaFisicaDto dto) {
        DetallePruebaFisica detalle = new DetallePruebaFisica();
        detalle.setIdDetalleAptitud(dto.getIdDetalleAptitud());
        detalle.setValorObtenido(dto.getValorObtenido());
        detalle.setPuntajeParcial(dto.getPuntajeParcial());
        if (dto.getIdPrueba() != null) {
            detalle.setCatalogoPrueba(CatalogoPrueba.builder().idPrueba(dto.getIdPrueba()).build());
        }
        return detalle;
    }
}