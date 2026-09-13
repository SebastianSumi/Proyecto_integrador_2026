package pe.edu.upeu.saludablemente.aptitudfisica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;
import pe.edu.upeu.saludablemente.aptitudfisica.mapper.AptitudFisicaMapper;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.CatalogoPruebaRepository;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoPruebaServiceImpl implements CatalogoPruebaService {

    private final CatalogoPruebaRepository catalogoPruebaRepository;
    private final AptitudFisicaMapper aptitudFisicaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoPruebaDto> listar(boolean soloActivos) {
        List<CatalogoPrueba> pruebas = soloActivos
                ? catalogoPruebaRepository.findByActivoTrue()
                : catalogoPruebaRepository.findAll();
        return pruebas.stream().map(aptitudFisicaMapper::toCatalogoPruebaDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoPruebaDto obtener(Long id) {
        return aptitudFisicaMapper.toCatalogoPruebaDto(buscarOFallar(id));
    }

    @Override
    @Transactional
    public CatalogoPruebaDto crear(CatalogoPruebaDto request) {
        CatalogoPrueba prueba = aptitudFisicaMapper.toCatalogoPrueba(request);
        prueba.setActivo(request.getActivo() == null || request.getActivo());
        return aptitudFisicaMapper.toCatalogoPruebaDto(catalogoPruebaRepository.save(prueba));
    }

    @Override
    @Transactional
    public CatalogoPruebaDto actualizar(Long id, CatalogoPruebaDto request) {
        CatalogoPrueba prueba = buscarOFallar(id);
        prueba.setNombrePrueba(request.getNombrePrueba());
        prueba.setUnidadMedida(request.getUnidadMedida());
        prueba.setDescripcion(request.getDescripcion());
        prueba.setActivo(request.getActivo());
        return aptitudFisicaMapper.toCatalogoPruebaDto(catalogoPruebaRepository.save(prueba));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        catalogoPruebaRepository.delete(buscarOFallar(id));
    }

    private CatalogoPrueba buscarOFallar(Long id) {
        return catalogoPruebaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prueba del catalogo no encontrada: " + id));
    }
}