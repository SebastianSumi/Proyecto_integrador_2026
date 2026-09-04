package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsignadorCargaService {

    private final AlertaClinicaRepository alertaRepository;

    // Cache de evaluadores disponibles (simulado - en producción vendría de un servicio de usuarios)
    private static final Map<Long, String> EVALUADORES_DISPONIBLES = new ConcurrentHashMap<>();

    static {
        // Simular evaluadores disponibles
        EVALUADORES_DISPONIBLES.put(1L, "Dr. Juan Perez");
        EVALUADORES_DISPONIBLES.put(2L, "Dra. Maria Lopez");
        EVALUADORES_DISPONIBLES.put(3L, "Dr. Carlos Ruiz");
        EVALUADORES_DISPONIBLES.put(4L, "Nut. Ana Torres");
        EVALUADORES_DISPONIBLES.put(5L, "Nut. Luis Garcia");
    }

    /**
     * Asigna un evaluador basado en la carga de trabajo
     */
    public Optional<Long> asignarEvaluador(
            EnrutamientoEspecialidadService.Especialidad especialidad) {

        log.debug(" Asignando evaluador para especialidad: {}", especialidad.getDisplayName());

        // Obtener evaluadores disponibles para la especialidad
        List<Long> evaluadoresIds = obtenerEvaluadoresPorEspecialidad(especialidad);

        if (evaluadoresIds.isEmpty()) {
            log.warn(" No hay evaluadores disponibles para especialidad: {}", especialidad.getDisplayName());
            return Optional.empty();
        }

        // Calcular carga de trabajo de cada evaluador
        Map<Long, Long> cargaPorEvaluador = new HashMap<>();
        List<EstadoAlerta> estadosPendientes = List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION);

        for (Long evaluadorId : evaluadoresIds) {
            long carga = alertaRepository.countAlertasPendientesByEvaluador(evaluadorId, estadosPendientes);
            cargaPorEvaluador.put(evaluadorId, carga);
        }

        // Encontrar el evaluador con menor carga
        Optional<Map.Entry<Long, Long>> evaluadorConMenorCarga = cargaPorEvaluador.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue());

        if (evaluadorConMenorCarga.isPresent()) {
            Long evaluadorId = evaluadorConMenorCarga.get().getKey();
            Long carga = evaluadorConMenorCarga.get().getValue();
            log.info(" Evaluador asignado: {} (carga: {} casos)",
                    EVALUADORES_DISPONIBLES.get(evaluadorId), carga);
            return Optional.of(evaluadorId);
        }

        // Si no hay evaluadores disponibles, asignar el primero
        if (!evaluadoresIds.isEmpty()) {
            log.warn("Asignando evaluador por defecto: {}", evaluadoresIds.get(0));
            return Optional.of(evaluadoresIds.get(0));
        }

        return Optional.empty();
    }

    /**
     * Obtiene los evaluadores disponibles para una especialidad
     */
    private List<Long> obtenerEvaluadoresPorEspecialidad(
            EnrutamientoEspecialidadService.Especialidad especialidad) {

        // TODO: En producción, consultar a un servicio de usuarios
        // Por ahora, simulamos evaluadores por especialidad
        return switch (especialidad) {
            case NUTRICION -> List.of(4L, 5L);
            case MEDICINA_OCUPACIONAL -> List.of(1L, 2L, 3L);
            case GENERAL -> List.of(1L, 2L, 3L, 4L, 5L);
        };
    }

    /**
     * Obtiene el nombre de un evaluador por su ID
     */
    public String getNombreEvaluador(Long idEvaluador) {
        return EVALUADORES_DISPONIBLES.getOrDefault(idEvaluador, "Evaluador " + idEvaluador);
    }

    /**
     * Registra un evaluador disponible
     */
    public void registrarEvaluador(Long idEvaluador, String nombre) {
        EVALUADORES_DISPONIBLES.put(idEvaluador, nombre);
        log.info("️ Evaluador registrado: {} - {}", idEvaluador, nombre);
    }

    /**
     * Obtiene la carga actual de un evaluador
     */
    public long getCargaEvaluador(Long idEvaluador) {
        List<EstadoAlerta> estadosPendientes = List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION);
        return alertaRepository.countAlertasPendientesByEvaluador(idEvaluador, estadosPendientes);
    }

    /**
     * Obtiene todos los evaluadores disponibles
     */
    public Map<Long, String> getEvaluadoresDisponibles() {
        return new HashMap<>(EVALUADORES_DISPONIBLES);
    }
}