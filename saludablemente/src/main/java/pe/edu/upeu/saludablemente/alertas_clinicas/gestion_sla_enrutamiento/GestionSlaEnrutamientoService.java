package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaDetalleEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.AlertaDetalleResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.AlertaInboxResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.dto.FiltroInboxRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service.AsignadorCargaService;
import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service.EnrutamientoEspecialidadService;
import pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionSlaEnrutamientoService {

    private final AlertaClinicaRepository alertaRepository;
    private final EnrutamientoEspecialidadService enrutamientoEspecialidadService;
    private final AsignadorCargaService asignadorCargaService;

    /**
     * Obtiene la bandeja de entrada del evaluador con filtros
     */
    public Page<AlertaInboxResponseDTO> obtenerBandejaInbox(FiltroInboxRequestDTO filtro) {
        log.debug(" Obteniendo bandeja de entrada para evaluador: {}", filtro.getIdEvaluador());

        // Construir especificación de filtros
        Specification<AlertaClinicaEntity> spec = construirFiltros(filtro);

        // Configurar paginación y ordenamiento
        Pageable pageable = PageRequest.of(
                filtro.getPagina(),
                filtro.getLimite(),
                Sort.by(Sort.Direction.ASC, "nivelSeveridad")
                        .and(Sort.by(Sort.Direction.ASC, "fechaVencimientoSla"))
        );

        // Ejecutar consulta
        Page<AlertaClinicaEntity> alertasPage = alertaRepository.findAll(spec, pageable);

        // Convertir a DTOs
        return alertasPage.map(this::convertirAInboxResponse);
    }

    /**
     * Obtiene el detalle completo de una alerta
     */
    public AlertaDetalleResponseDTO obtenerDetalleAlerta(UUID idAlerta) {
        log.debug(" Obteniendo detalle de alerta: {}", idAlerta);

        AlertaClinicaEntity alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada: " + idAlerta));

        return convertirADetalleResponse(alerta);
    }

    /**
     * Enruta y asigna una nueva alerta
     */
    @Transactional
    public Optional<Long> enrutarYAsignarAlerta(UUID idAlerta) {
        log.info(" Enrutando y asignando alerta: {}", idAlerta);

        AlertaClinicaEntity alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada: " + idAlerta));

        // 1. Determinar especialidad
        Set<TipoIndicador> indicadores = alerta.getDetalles().stream()
                .map(AlertaClinicaDetalleEntity::getTipoIndicador)
                .collect(Collectors.toSet());

        EnrutamientoEspecialidadService.Especialidad especialidad =
                enrutamientoEspecialidadService.determinarEspecialidadPorIndicadores(indicadores);

        log.info(" Especialidad determinada: {}", especialidad.getDisplayName());

        // 2. Asignar evaluador
        Optional<Long> evaluadorId = asignadorCargaService.asignarEvaluador(especialidad);

        if (evaluadorId.isPresent()) {
            log.info("️ Evaluador asignado: {}", evaluadorId.get());
            // El evaluador se asignará en la resolución (cuando se atienda la alerta)
            // Por ahora solo guardamos la especialidad
            alerta.setEstado(EstadoAlerta.EN_REVISION);
            alertaRepository.save(alerta);
        }

        return evaluadorId;
    }

    /**
     * Construye especificación para filtros
     */
    private Specification<AlertaClinicaEntity> construirFiltros(FiltroInboxRequestDTO filtro) {
        Specification<AlertaClinicaEntity> spec = Specification.unrestricted();

        if (filtro.getEstado() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("estado"), filtro.getEstado()));
        } else {
            // Por defecto, mostrar alertas pendientes y en revisión
            spec = spec.and((root, query, cb) ->
                    root.get("estado").in(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION));
        }

        if (filtro.getNivelSeveridad() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("nivelSeveridad"), filtro.getNivelSeveridad()));
        }

        if (filtro.getTipoIndicador() != null) {
            spec = spec.and((root, query, cb) -> {
                var detalles = root.join("detalles");
                return cb.equal(detalles.get("tipoIndicador"), filtro.getTipoIndicador());
            });
        }

        if (filtro.getNombrePaciente() != null && !filtro.getNombrePaciente().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var persona = root.join("persona");
                return cb.or(
                        cb.like(cb.lower(persona.get("nombre")),
                                "%" + filtro.getNombrePaciente().toLowerCase() + "%"),
                        cb.like(cb.lower(persona.get("apellido")),
                                "%" + filtro.getNombrePaciente().toLowerCase() + "%")
                );
            });
        }

        if (Boolean.TRUE.equals(filtro.getSoloVencidos())) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("fechaVencimientoSla"), LocalDateTime.now()));
        }

        // Si hay ID de evaluador, filtrar por sus alertas asignadas
        if (filtro.getIdEvaluador() != null) {
            spec = spec.and((root, query, cb) -> {
                var resolucion = root.join("resolucion", jakarta.persistence.criteria.JoinType.LEFT);
                return cb.equal(resolucion.get("idUsuarioEvaluador"), filtro.getIdEvaluador());
            });
        }

        return spec;
    }

    /**
     * Convierte una entidad a DTO de bandeja
     */
    private AlertaInboxResponseDTO convertirAInboxResponse(AlertaClinicaEntity alerta) {
        // Obtener primer indicador de la alerta
        TipoIndicador tipoIndicador = alerta.getDetalles().isEmpty() ?
                null : alerta.getDetalles().get(0).getTipoIndicador();

        // Calcular horas restantes de SLA
        Long horasSlaRestantes = null;
        if (alerta.getFechaVencimientoSla() != null) {
            horasSlaRestantes = ChronoUnit.HOURS.between(
                    LocalDateTime.now(), alerta.getFechaVencimientoSla());
        }

        return AlertaInboxResponseDTO.builder()
                .idAlerta(alerta.getIdAlerta())
                .nombrePaciente(alerta.getPersona().getNombreCompleto())
                .areaTrabajo(alerta.getPersona().getAreaTrabajo())
                .tipoIndicador(tipoIndicador)
                .nivelSeveridad(alerta.getNivelSeveridad())
                .estado(alerta.getEstado())
                .fechaGeneracion(alerta.getFechaGeneracion())
                .fechaVencimientoSla(alerta.getFechaVencimientoSla())
                .horasSlaRestantes(horasSlaRestantes)
                .esVencida(alerta.isVencida())
                .build();
    }

    /**
     * Convierte una entidad a DTO de detalle
     */
    private AlertaDetalleResponseDTO convertirADetalleResponse(AlertaClinicaEntity alerta) {
        // Convertir detalles a IndicadorEvaluadoDTO
        List<IndicadorEvaluadoDTO> detallesClinicos = alerta.getDetalles().stream()
                .map(detalle -> IndicadorEvaluadoDTO.builder()
                        .tipoIndicador(detalle.getTipoIndicador())
                        .valorMedido(detalle.getValorMedido())
                        .limiteReferencia(detalle.getLimiteReferencia())
                        .porcentajeDesviacion(detalle.getPorcentajeDesviacion())
                        .multiplicadorReincidencia(detalle.getMultiplicadorReincidencia())
                        .build())
                .collect(Collectors.toList());

        // Obtener síndrome del snapshot
        String sindromeDetectado = null;
        if (alerta.getSnapshotInmutable() != null) {
            Object sindrome = alerta.getSnapshotInmutable().get("sindromeDetectado");
            if (sindrome != null) {
                sindromeDetectado = sindrome.toString();
            }
        }

        // Calcular horas restantes de SLA
        Long horasSlaRestantes = null;
        if (alerta.getFechaVencimientoSla() != null) {
            horasSlaRestantes = ChronoUnit.HOURS.between(
                    LocalDateTime.now(), alerta.getFechaVencimientoSla());
        }

        return AlertaDetalleResponseDTO.builder()
                .idAlerta(alerta.getIdAlerta())
                .idEvaluacionOrigen(alerta.getIdEvaluacionOrigen())
                .nombrePaciente(alerta.getPersona().getNombreCompleto())
                .edad(alerta.getPersona().getEdad())
                .sexo(alerta.getPersona().getSexo())
                .nivelSeveridad(alerta.getNivelSeveridad())
                .scoreRiesgo(alerta.getScoreRiesgo())
                .sindromeDetectado(sindromeDetectado)
                .detallesClinicos(detallesClinicos)
                .estado(alerta.getEstado())
                .fechaGeneracion(alerta.getFechaGeneracion())
                .fechaVencimientoSla(alerta.getFechaVencimientoSla())
                .horasSlaRestantes(horasSlaRestantes)
                .build();
    }

    /**
     * Obtiene estadísticas de alertas para el dashboard
     */
    public Map<String, Object> obtenerEstadisticas(Long idEvaluador) {
        Map<String, Object> estadisticas = new HashMap<>();

        long totalPendientes = alertaRepository.countByEstado(EstadoAlerta.PENDIENTE);
        long totalEnRevision = alertaRepository.countByEstado(EstadoAlerta.EN_REVISION);
        long totalCriticas = alertaRepository.countByNivelSeveridadAndEstado(
                pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad.CRITICO,
                EstadoAlerta.PENDIENTE
        );

        estadisticas.put("totalPendientes", totalPendientes);
        estadisticas.put("totalEnRevision", totalEnRevision);
        estadisticas.put("totalCriticas", totalCriticas);
        estadisticas.put("cargaEvaluador", asignadorCargaService.getCargaEvaluador(idEvaluador));

        return estadisticas;
    }
}