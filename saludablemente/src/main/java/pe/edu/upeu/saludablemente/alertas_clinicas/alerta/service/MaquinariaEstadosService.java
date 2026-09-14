package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.TransicionEstadoInvalidaException;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class MaquinariaEstadosService {

    private static final EnumMap<EstadoAlerta, Set<EstadoAlerta>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoAlerta.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.PENDIENTE,
                Set.of(EstadoAlerta.EN_REVISION, EstadoAlerta.ATENDIDA, EstadoAlerta.DESESTIMADA, EstadoAlerta.VENCIDO, EstadoAlerta.ANULADA));
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.EN_REVISION,
                Set.of(EstadoAlerta.ATENDIDA, EstadoAlerta.DESESTIMADA, EstadoAlerta.VENCIDO, EstadoAlerta.ANULADA));
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.VENCIDO,
                Set.of(EstadoAlerta.ATENDIDA, EstadoAlerta.DESESTIMADA, EstadoAlerta.ANULADA));
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.ATENDIDA, new HashSet<>());
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.DESESTIMADA, new HashSet<>());
        TRANSICIONES_PERMITIDAS.put(EstadoAlerta.ANULADA, new HashSet<>());
    }

    public AlertaClinicaEntity transitarEstado(AlertaClinicaEntity alerta, EstadoAlerta nuevoEstado)
            throws TransicionEstadoInvalidaException {

        if (alerta == null) throw new IllegalArgumentException("La alerta no puede ser nula");
        EstadoAlerta estadoActual = alerta.getEstado();
        if (estadoActual == null) throw new IllegalArgumentException("La alerta no tiene estado definido");

        if (estadoActual == nuevoEstado) {
            log.debug("Alerta {} ya está en estado {}", alerta.getIdAlerta(), estadoActual);
            return alerta;
        }

        if (!isTransicionPermitida(estadoActual, nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    String.format("No se permite transición de %s a %s para la alerta %s",
                            estadoActual, nuevoEstado, alerta.getIdAlerta())
            );
        }

        validarTransicion(estadoActual, nuevoEstado, alerta);

        log.info("Transicionando alerta {} de {} a {}", alerta.getIdAlerta(), estadoActual, nuevoEstado);
        alerta.setEstado(nuevoEstado);
        return alerta;
    }

    public boolean isTransicionPermitida(EstadoAlerta origen, EstadoAlerta destino) {
        if (origen == null || destino == null) return false;
        return TRANSICIONES_PERMITIDAS.getOrDefault(origen, new HashSet<>()).contains(destino);
    }

    private void validarTransicion(EstadoAlerta origen, EstadoAlerta destino, AlertaClinicaEntity alerta)
            throws TransicionEstadoInvalidaException {

        if (isEstadoTerminal(origen)) {
            throw new TransicionEstadoInvalidaException("La alerta ya está en estado terminal: " + origen);
        }

        if (destino == EstadoAlerta.DESESTIMADA) {
            log.warn("Desestimando alerta: {}", alerta.getIdAlerta());
        }
    }

    public boolean isEstadoTerminal(EstadoAlerta estado) {
        return estado == EstadoAlerta.ATENDIDA || estado == EstadoAlerta.DESESTIMADA || estado == EstadoAlerta.ANULADA;
    }
}
