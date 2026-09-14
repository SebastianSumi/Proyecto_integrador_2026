package pe.edu.upeu.saludablemente.aptitudfisica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.DetallePruebaFisicaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.CatalogoPrueba;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.DetallePruebaFisica;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.EvaluacionAptitud;
import pe.edu.upeu.saludablemente.aptitudfisica.mapper.AptitudFisicaMapper;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.CatalogoPruebaRepository;
import pe.edu.upeu.saludablemente.aptitudfisica.repository.EvaluacionAptitudRepository;
import pe.edu.upeu.saludablemente.exception.BusinessRuleException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionAptitudServiceImpl implements EvaluacionAptitudService {

    private static final BigDecimal PUNTAJE_MAXIMO_PRUEBA = BigDecimal.valueOf(20);

    private final EvaluacionAptitudRepository evaluacionAptitudRepository;
    private final CatalogoPruebaRepository catalogoPruebaRepository;
    private final PersonaService personaService;
    private final AptitudFisicaMapper aptitudFisicaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionAptitudResponseDto> listar(Long personaId, Boolean sincronizado, LocalDate desde, LocalDate hasta) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionAptitudRepository.buscar(personaId, sincronizado, desde, hasta, Sort.by("id"))
                .stream()
                .map(aptitudFisicaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionAptitudResumenDto> listarResumen(Long personaId, Boolean sincronizado, LocalDate desde, LocalDate hasta) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionAptitudRepository.buscarResumen(personaId, sincronizado, desde, hasta, Sort.by("id"));
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionAptitudAgregadoDto obtenerAgregados(Long personaId) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionAptitudRepository.agregados(personaId);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionAptitudResponseDto obtener(Long idEvaluacionAptitud) {
        return aptitudFisicaMapper.toResponse(buscarOFallar(idEvaluacionAptitud));
    }

    @Override
    @Transactional
    public EvaluacionAptitudResponseDto registrarEvaluacion(EvaluacionAptitudRequestDto request) {
        personaService.obtener(request.getIdPersona());

        EvaluacionAptitud evaluacion = aptitudFisicaMapper.toEntity(request);
        evaluacion.getDetalles().clear();

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        for (DetallePruebaFisicaDto detalleDto : request.getDetalles()) {
            CatalogoPrueba prueba = catalogoPruebaRepository.findById(detalleDto.getIdPrueba())
                    .orElseThrow(() -> new BusinessRuleException(
                            "La prueba con id " + detalleDto.getIdPrueba() + " no existe en el catalogo"));

            DetallePruebaFisica detalle = aptitudFisicaMapper.toDetalle(detalleDto);
            detalle.setCatalogoPrueba(prueba);
            detalle.setEvaluacionAptitud(evaluacion);
            if (detalle.getPuntajeParcial() == null) {
                detalle.setPuntajeParcial(BigDecimal.ZERO);
            }
            puntajeTotal = puntajeTotal.add(detalle.getPuntajeParcial());
            evaluacion.getDetalles().add(detalle);
        }

        evaluacion.setPuntajeGlobal(puntajeTotal.setScale(2, RoundingMode.HALF_UP));
        evaluacion.setDiagnosticoAptitud(diagnosticar(puntajeTotal, request.getDetalles().size()));
        aplicarEstadoSincronizacion(evaluacion, request);
        return aptitudFisicaMapper.toResponse(evaluacionAptitudRepository.save(evaluacion));
    }

    @Override
    @Transactional
    public void eliminar(Long idEvaluacionAptitud) {
        evaluacionAptitudRepository.delete(buscarOFallar(idEvaluacionAptitud));
    }

    private void validarPersonaSiCorresponde(Long personaId) {
        if (personaId != null) {
            personaService.obtener(personaId);
        }
    }

    private EvaluacionAptitud buscarOFallar(Long idEvaluacionAptitud) {
        return evaluacionAptitudRepository.findById(idEvaluacionAptitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluacion de aptitud fisica no encontrada: " + idEvaluacionAptitud));
    }

    private String diagnosticar(BigDecimal puntajeTotal, int cantidad) {
        if (cantidad == 0) {
            return "SIN EVALUACION";
        }
        BigDecimal promedio = puntajeTotal.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        BigDecimal porcentaje = promedio.divide(PUNTAJE_MAXIMO_PRUEBA, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (porcentaje.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "Excelente";
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "Bueno";
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Regular";
        }
        return "Deficiente";
    }

    private void aplicarEstadoSincronizacion(EvaluacionAptitud evaluacion, EvaluacionAptitudRequestDto request) {
        boolean sincronizado = request.getSincronizado() == null || request.getSincronizado();
        evaluacion.setSincronizado(sincronizado);
        evaluacion.setFechaSincronizacion(sincronizado
                ? (request.getFechaSincronizacion() != null ? request.getFechaSincronizacion() : LocalDateTime.now())
                : null);
    }
}