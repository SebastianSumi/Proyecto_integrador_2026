package pe.edu.upeu.saludablemente.aptitudfisica.service;

import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;

import java.util.List;

public interface CatalogoPruebaService {

    List<CatalogoPruebaDto> listar(Boolean activo, String nombre);

    CatalogoPruebaDto obtener(Long id);

    CatalogoPruebaDto crear(CatalogoPruebaDto request);

    CatalogoPruebaDto actualizar(Long id, CatalogoPruebaDto request);

    void eliminar(Long id);
}