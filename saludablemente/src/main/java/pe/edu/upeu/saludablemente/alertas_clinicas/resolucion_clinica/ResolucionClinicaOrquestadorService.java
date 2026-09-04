package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception.TransicionEstadoInvalidaException;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.CatalogoAccionCorrectivaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.repository.CatalogoAccionRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.repository.ResolucionClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service.DesestimacionService;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service.ProgramadorSeguimientoService;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service.TrazabilidadResponsableService;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.RecursoNoEncontradoException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolucionClinicaOrquestadorService {

    private final CicloVidaAlertaService cicloVidaAlertaService;
    private final ResolucionClinicaRepository resolucionRepository;
    private final CatalogoAccionRepository catalogoAccionRepository;
    private final TrazabilidadResponsableService trazabilidadResponsableService;
    private final DesestimacionService desestimacionService;
    private final ProgramadorSeguimientoService programadorSeguimientoService;

    /**
     * Atiende una alerta (marca como ATENDIDA)
     */
    @Transactional
    public ResolucionClinicaEntity atenderAlerta(
            AtenderAlertaRequestDTO request,
            Long idUsuarioEvaluador,
            String nombreEvaluador) {

        log.info("🏥 Atendiendo alerta: {} por evaluador: {}", request.getIdAlerta(), nombreEvaluador);

        // 1. Validar que la alerta existe
        AlertaClinicaEntity alerta = cicloVidaAlertaService.obtenerAlerta(request.getIdAlerta());

        // 2. Validar que la alerta no esté ya resuelta
        if (resolucionRepository.existsByAlerta_IdAlerta(request.getIdAlerta())) {
            throw new ResolucionYaExisteException(request.getIdAlerta());
        }

        // 3. Validar que el estado sea válido para atención
        if (alerta.getEstado() == EstadoAlerta.ATENDIDA ||
                alerta.getEstado() == EstadoAlerta.DESESTIMADA) {
            throw new TransicionEstadoInvalidaException(
                    "La alerta ya se encuentra en estado terminal: " + alerta.getEstado()
            );
        }

        // 4. Obtener la acción correctiva
        CatalogoAccionCorrectivaEntity accionCorrectiva = catalogoAccionRepository
                .findById(request.getIdAccionCorrectiva())
                .orElseThrow(() -> new CatalogoClinicoIncompletoException(
                        String.valueOf(request.getIdAccionCorrectiva()))
                );

        // 5. Crear la resolución
        ResolucionClinicaEntity resolucion = ResolucionClinicaEntity.builder()
                .alerta(alerta)
                .accionCorrectiva(accionCorrectiva)
                .observacionesClinicas(request.getObservacionesClinicas())
                .build();

        // 6. Sellar con trazabilidad
        resolucion = trazabilidadResponsableService.sellarConTrazabilidad(
                resolucion, idUsuarioEvaluador, nombreEvaluador
        );

        // 7. Programar seguimiento si aplica
        if (programadorSeguimientoService.requiereSeguimiento(accionCorrectiva)) {
            resolucion = programadorSeguimientoService.programarSeguimiento(
                    resolucion, accionCorrectiva
            );
        }

        // 8. Guardar resolución
        ResolucionClinicaEntity resolucionGuardada = resolucionRepository.save(resolucion);

        // 9. Cambiar estado de la alerta
        cicloVidaAlertaService.cambiarEstado(alerta.getIdAlerta(), EstadoAlerta.ATENDIDA);

        log.info("✅ Alerta {} atendida exitosamente. Resolución: {}",
                alerta.getIdAlerta(), resolucionGuardada.getIdResolucion());

        // 10. Si tiene seguimiento programado, generar evento
        if (Boolean.TRUE.equals(resolucionGuardada.getEsSeguimientoProgramado())) {
            programadorSeguimientoService.generarAlertaSeguimiento(resolucionGuardada);
        }

        return resolucionGuardada;
    }

    /**
     * Desestima una alerta (marca como DESESTIMADA)
     */
    @Transactional
    public ResolucionClinicaEntity desestimarAlerta(
            DesestimarAlertaRequestDTO request,
            Long idUsuarioEvaluador,
            String nombreEvaluador) {

        log.info("❌ Desestimando alerta: {} por evaluador: {}", request.getIdAlerta(), nombreEvaluador);

        // 1. Validar que la alerta existe
        AlertaClinicaEntity alerta = cicloVidaAlertaService.obtenerAlerta(request.getIdAlerta());

        // 2. Validar que la alerta no esté ya resuelta
        if (resolucionRepository.existsByAlerta_IdAlerta(request.getIdAlerta())) {
            throw new ResolucionYaExisteException(request.getIdAlerta());
        }

        // 3. Validar motivo de desestimación
        desestimacionService.validarMotivoDesestimacion(request);

        // 4. Crear la resolución
        ResolucionClinicaEntity resolucion = ResolucionClinicaEntity.builder()
                .alerta(alerta)
                .observacionesClinicas(request.getObservacionesClinicas())
                .build();

        // 5. Registrar desestimación
        resolucion = desestimacionService.registrarDesestimacion(resolucion, request);

        // 6. Sellar con trazabilidad
        resolucion = trazabilidadResponsableService.sellarConTrazabilidad(
                resolucion, idUsuarioEvaluador, nombreEvaluador
        );

        // 7. Guardar resolución
        ResolucionClinicaEntity resolucionGuardada = resolucionRepository.save(resolucion);

        // 8. Cambiar estado de la alerta
        cicloVidaAlertaService.cambiarEstado(alerta.getIdAlerta(), EstadoAlerta.DESESTIMADA);

        log.info("✅ Alerta {} desestimada exitosamente. Motivo: {}",
                alerta.getIdAlerta(), request.getMotivoDesestimacion());

        return resolucionGuardada;
    }

    /**
     * Obtiene el catálogo de acciones correctivas
     */
    public List<CatalogoAccionResponseDTO> obtenerCatalogoAcciones() {
        log.debug("📋 Obteniendo catálogo de acciones correctivas");

        return catalogoAccionRepository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Obtiene una resolución por ID de alerta
     */
    public ResolucionClinicaEntity obtenerResolucionPorAlerta(UUID idAlerta) {
        return resolucionRepository.findByAlerta_IdAlerta(idAlerta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Resolución para alerta", idAlerta.toString()));
    }

    /**
     * Convierte una entidad a DTO
     */
    private CatalogoAccionResponseDTO convertirADTO(CatalogoAccionCorrectivaEntity entity) {
        return CatalogoAccionResponseDTO.builder()
                .idAccion(entity.getIdAccion())
                .codigoTipificado(entity.getCodigoTipificado())
                .descripcion(entity.getDescripcion())
                .requiereSeguimiento(entity.isRequiereSeguimiento())
                .diasParaSeguimiento(entity.getDiasSeguimiento())
                .build();
    }

    /**
     * Obtiene los motivos válidos para desestimación
     */
    public List<String> obtenerMotivosDesestimacion() {
        return desestimacionService.getMotivosValidos().stream()
                .sorted()
                .toList();
    }
}