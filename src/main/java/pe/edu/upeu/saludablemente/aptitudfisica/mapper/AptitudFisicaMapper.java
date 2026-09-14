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

    @Mapping(target = "idPrueba", source = "id")
    CatalogoPruebaDto toCatalogoPruebaDto(CatalogoPrueba catalogoPrueba);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    CatalogoPrueba toCatalogoPrueba(CatalogoPruebaDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    EvaluacionAptitud toEntity(EvaluacionAptitudRequestDto request);

    @Mapping(target = "idDetalleAptitud", source = "id")
    @Mapping(target = "idPrueba", source = "catalogoPrueba.id")
    @Mapping(target = "nombrePrueba", source = "catalogoPrueba.nombrePrueba")
    DetallePruebaFisicaDto toDetalleDto(DetallePruebaFisica detalle);

    @Mapping(target = "idEvaluacionAptitud", source = "id")
    EvaluacionAptitudResponseDto toResponse(EvaluacionAptitud evaluacion);

    default DetallePruebaFisica toDetalle(DetallePruebaFisicaDto dto) {
        DetallePruebaFisica detalle = new DetallePruebaFisica();
        detalle.setId(dto.getIdDetalleAptitud());
        detalle.setValorObtenido(dto.getValorObtenido());
        detalle.setPuntajeParcial(dto.getPuntajeParcial());
        if (dto.getIdPrueba() != null) {
            CatalogoPrueba prueba = new CatalogoPrueba();
            prueba.setId(dto.getIdPrueba());
            detalle.setCatalogoPrueba(prueba);
        }
        return detalle;
    }
}