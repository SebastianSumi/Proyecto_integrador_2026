package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception.TransicionEstadoInvalidaException;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class MaquinariaEstadosService {

    // Mapa de transiciones permitidas entre estados
    private static final EnumMap<EstadoAlerta, Set<EstadoAlerta>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoAlerta.class);

    static {
        // Estado PENDIENTE puede transicionar a:
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.PENDIENTE,
                Set.of(EstadoAlerta.EN_REVISION, EstadoAlerta.DESESTIMADA, EstadoAlerta.VENCIDO));

        // Estado EN_REVISION puede transicionar a:
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.EN_REVISION,
                Set.of(EstadoAlerta.ATENDIDA, EstadoAlerta.DESESTIMADA, EstadoAlerta.VENCIDO));

        // Estado ATENDIDA es terminal (no puede transicionar a ningún otro)
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.ATENDIDA, new HashSet<>());

        // Estado DESESTIMADA es terminal (no puede transicionar a ningún otro)
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.DESESTIMADA, new HashSet<>());

        // Estado VENCIDO puede transicionar a:
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.VENCIDO,
                Set.of(EstadoAlerta.ATENDIDA, EstadoAlerta.DESESTIMADA));
    }

    /**
     * Valida y realiza la transición de estado de una alerta
     */
    public AlertaClinicaEntity transitarEstado(AlertaClinicaEntity alerta, EstadoAlerta nuevoEstado)
            {

        if (alerta == null) {
            throw new IllegalArgumentException("La alerta no puede ser nula");
        }

        EstadoAlerta estadoActual = alerta.getEstado();

        if (estadoActual == null) {
            throw new IllegalArgumentException("La alerta no tiene un estado definido");
        }

        if (estadoActual == nuevoEstado) {
            log.debug("La alerta {} ya está en estado {}", alerta.getIdAlerta(), estadoActual);
            return alerta;
        }

        // Verificar si la transición está permitida
        if (!isTransicionPermitida(estadoActual, nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    String.format("No se permite transición de %s a %s para la alerta %s",
                            estadoActual, nuevoEstado, alerta.getIdAlerta())
            );
        }

        // Validaciones adicionales según el estado destino
        validarTransicion(estadoActual, nuevoEstado, alerta);

        // Realizar la transición
        log.info("🔄 Transicionando alerta {} de {} a {}",
                alerta.getIdAlerta(), estadoActual, nuevoEstado);
        alerta.setEstado(nuevoEstado);

        return alerta;
    }

    /**
     * Verifica si una transición está permitida
     */
    public boolean isTransicionPermitida(EstadoAlerta origen, EstadoAlerta destino) {
        if (origen == null || destino == null) {
            return false;
        }

        Set<EstadoAlerta> transicionesPermitidas = TRANSICIONES_PERMITIDAS.get(origen);
        return transicionesPermitidas != null && transicionesPermitidas.contains(destino);
    }

    /**
     * Realiza validaciones adicionales para la transición
     */
    private void validarTransicion(EstadoAlerta origen, EstadoAlerta destino, AlertaClinicaEntity alerta)
            throws TransicionEstadoInvalidaException {

        // No se puede atender una alerta que ya está vencida sin justificación
        if (destino == EstadoAlerta.ATENDIDA && origen == EstadoAlerta.VENCIDO) {
            log.warn("⚠️ Atendiendo alerta vencida: {}", alerta.getIdAlerta());
        }

        // No se puede desestimar sin motivo (se validará en ResolucionClinica)
        if (destino == EstadoAlerta.DESESTIMADA) {
            log.warn("⚠️ Desestimando alerta: {}", alerta.getIdAlerta());
        }

        // Si la alerta ya fue atendida o desestimada, no se puede modificar
        if (origen == EstadoAlerta.ATENDIDA || origen == EstadoAlerta.DESESTIMADA) {
            throw new TransicionEstadoInvalidaException(
                    "La alerta ya se encuentra en estado terminal: " + origen
            );
        }
    }

    /**
     * Verifica si un estado es terminal (no admite más transiciones)
     */
    public boolean isEstadoTerminal(EstadoAlerta estado) {
        return estado == EstadoAlerta.ATENDIDA || estado == EstadoAlerta.DESESTIMADA;
    }

    /**
     * Verifica si un estado es pendiente (requiere acción)
     */
    public boolean isEstadoPendiente(EstadoAlerta estado) {
        return estado == EstadoAlerta.PENDIENTE || estado == EstadoAlerta.EN_REVISION;
    }

    /**
     * Obtiene los estados permitidos para una transición desde un estado origen
     */
    public Set<EstadoAlerta> getEstadosPermitidos(EstadoAlerta origen) {
        return TRANSICIONES_PERMITIDAS.getOrDefault(origen, new HashSet<>());
    }

    /**
     * Inicializa una alerta en estado PENDIENTE
     */
    public AlertaClinicaEntity inicializarAlerta(AlertaClinicaEntity alerta) {
        if (alerta == null) {
            throw new IllegalArgumentException("La alerta no puede ser nula");
        }

        if (alerta.getEstado() == null) {
            alerta.setEstado(EstadoAlerta.PENDIENTE);
            log.info("📝 Alerta {} inicializada en estado PENDIENTE", alerta.getIdAlerta());
        }

        return alerta;
    }

    /**
     * Obtiene el siguiente estado recomendado según la severidad
     */
    public EstadoAlerta getEstadoRecomendadoPorSeveridad(pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad severidad) {
        if (severidad == pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad.CRITICO) {
            return EstadoAlerta.EN_REVISION;
        }
        return EstadoAlerta.PENDIENTE;
    }
}