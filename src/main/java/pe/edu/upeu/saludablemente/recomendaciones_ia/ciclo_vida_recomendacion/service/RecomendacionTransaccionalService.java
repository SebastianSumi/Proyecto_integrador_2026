package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.service.AuditoriaService;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto.*;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionDetalleEntity;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionIAEntity;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.mapper.RecomendacionMapper;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository.RecomendacionDetalleRepository;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository.RecomendacionIARepository;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.EstadoInferencia;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.TipoSeccionRecomendacion;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception.RecomendacionNoVigenteException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionTransaccionalService {

    private final RecomendacionIARepository recomendacionRepository;
    private final RecomendacionDetalleRepository detalleRepository;
    private final VigenciaVersionadoService vigenciaVersionadoService;
    private final RecomendacionMapper mapper;
    private final AuditoriaService auditoriaService;

    @Transactional
    public RecomendacionIAEntity guardarRecomendacion(
            Long idPersona,
            Long idEvaluacion,
            String modeloIaUsado,
            String promptContexto,
            Map<String, Object> contenidoEjercicio,
            Map<String, Object> contenidoNutricion,
            String contraindicaciones,
            Integer scoreConfianza,
            Integer tiempoInferenciaMs,
            EstadoInferencia estadoInferencia) {

        log.info(" Guardando recomendacion para persona: {}", idPersona);

        vigenciaVersionadoService.desactivarRecomendacionesAnteriores(idPersona);

        RecomendacionIAEntity recomendacion = RecomendacionIAEntity.builder()
                .idPersona(idPersona)
                .idEvaluacion(idEvaluacion)
                .modeloIaUsado(modeloIaUsado)
                .promptContexto(promptContexto)
                .scoreConfianzaIa(scoreConfianza)
                .vigente(true)
                .leida(false)
                .tiempoInferenciaMs(tiempoInferenciaMs)
                .estadoInferencia(estadoInferencia)
                .build();

        RecomendacionIAEntity recomendacionGuardada = recomendacionRepository.save(recomendacion);

        if (contenidoEjercicio != null) {
            RecomendacionDetalleEntity detalleEjercicio = RecomendacionDetalleEntity.builder()
                    .recomendacion(recomendacionGuardada)
                    .tipoSeccion(TipoSeccionRecomendacion.EJERCICIO)
                    .contenidoEstructurado(contenidoEjercicio)
                    .contraindicaciones(contraindicaciones)
                    .build();
            detalleRepository.save(detalleEjercicio);
        }

        if (contenidoNutricion != null) {
            RecomendacionDetalleEntity detalleNutricion = RecomendacionDetalleEntity.builder()
                    .recomendacion(recomendacionGuardada)
                    .tipoSeccion(TipoSeccionRecomendacion.NUTRICION)
                    .contenidoEstructurado(contenidoNutricion)
                    .contraindicaciones(contraindicaciones)
                    .build();
            detalleRepository.save(detalleNutricion);
        }

        log.info(" Recomendacion guardada exitosamente: {}", recomendacionGuardada.getIdRecomendacion());
        registrarAuditoriaRecomendacion(recomendacionGuardada);
        return recomendacionGuardada;
    }

    private void registrarAuditoriaRecomendacion(RecomendacionIAEntity recomendacion) {
        try {
            auditoriaService.registrarInferenciaIA(
                    recomendacion.getIdRecomendacion() != null ? recomendacion.getIdRecomendacion().toString() : null,
                    recomendacion.getIdPersona(),
                    recomendacion.getModeloIaUsado(),
                    recomendacion.getPromptContexto(),
                    recomendacion.getTiempoInferenciaMs()
            );
            log.debug("Auditoria registrada para recomendacion {}", recomendacion.getIdRecomendacion());
        } catch (Exception e) {
            log.error("Error al registrar auditoria de recomendacion: {}", e.getMessage(), e);
        }
    }

    public RecomendacionVigenteResponseDTO obtenerRecomendacionVigente(Long idPersona) {
        log.debug(" Obteniendo recomendacion vigente para persona: {}", idPersona);

        return recomendacionRepository.findByIdPersonaAndVigenteTrue(idPersona)
                .map(mapper::toVigenteResponseDTO)
                .orElse(null);
    }

    public List<RecomendacionHistorialItemDTO> obtenerHistorialRecomendaciones(Long idPersona) {
        log.debug(" Obteniendo historial de recomendaciones para persona: {}", idPersona);

        return recomendacionRepository.findByIdPersonaOrderByFechaGeneracionDesc(idPersona)
                .stream()
                .map(mapper::toHistorialItemDTO)
                .toList();
    }

    public RecomendacionAuditoriaDetalleDTO obtenerDetalleAuditoria(UUID idRecomendacion) {
        log.debug(" Obteniendo detalle de auditoria para recomendacion: {}", idRecomendacion);

        RecomendacionIAEntity entity = recomendacionRepository.findById(idRecomendacion)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacion no encontrada: " + idRecomendacion));

        return mapper.toAuditoriaDetalleDTO(entity);
    }

    @Transactional
    public void confirmarLectura(UUID idRecomendacion) {
        log.info(" Confirmando lectura de recomendacion: {}", idRecomendacion);

        RecomendacionIAEntity entity = recomendacionRepository.findById(idRecomendacion)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacion no encontrada: " + idRecomendacion));

        if (!entity.isActiva()) {
            throw new RecomendacionNoVigenteException(
                    "La recomendacion ya no esta vigente. Solo se puede marcar como leida la recomendacion activa."
            );
        }

        entity.marcarComoLeida();
        recomendacionRepository.save(entity);
        log.info(" Recomendacion {} marcada como leida", idRecomendacion);
    }

    public List<RecomendacionIAEntity> obtenerRecomendacionesPorEstado(EstadoInferencia estado) {
        return recomendacionRepository.findByEstadoInferencia(estado);
    }
}
