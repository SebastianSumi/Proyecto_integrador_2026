package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaDetalleEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsolidacionDuplicadosService {

    private final AlertaClinicaRepository alertaRepository;
    private static final int HORAS_VENTANA = 24;

    public Optional<AlertaClinicaEntity> encontrarDuplicado(Long idPersona, TipoIndicador tipoIndicador, LocalDateTime fechaGeneracion) {
        if (idPersona == null || tipoIndicador == null) return Optional.empty();

        LocalDateTime fechaLimite = fechaGeneracion != null ?
                fechaGeneracion.minusHours(HORAS_VENTANA) :
                LocalDateTime.now().minusHours(HORAS_VENTANA);

        List<AlertaClinicaEntity> duplicados = alertaRepository.findAlertasDuplicadasPendientes(
                idPersona,
                tipoIndicador,
                List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION),
                fechaLimite
        );

        if (duplicados.isEmpty()) return Optional.empty();

        AlertaClinicaEntity duplicado = duplicados.stream()
                .max((a1, a2) -> a1.getFechaGeneracion().compareTo(a2.getFechaGeneracion()))
                .orElse(null);

        if (duplicado != null) {
            log.warn("Alerta duplicada encontrada para paciente {} y tipo {}: {}",
                    idPersona, tipoIndicador, duplicado.getIdAlerta());
        }

        return Optional.ofNullable(duplicado);
    }

    @Transactional
    public AlertaClinicaEntity consolidarConDuplicado(AlertaClinicaEntity nuevo, AlertaClinicaEntity existente) {
        if (nuevo == null || existente == null) {
            throw new IllegalArgumentException("Las alertas no pueden ser nulas para consolidación");
        }

        log.info("Consolidando alerta nueva con duplicado existente {}", existente.getIdAlerta());

        existente.getDetalles().clear();
        for (AlertaClinicaDetalleEntity detalle : nuevo.getDetalles()) {
            detalle.setAlerta(existente);
            existente.getDetalles().add(detalle);
        }

        if (nuevo.getSnapshotInmutable() != null) {
            existente.setSnapshotInmutable(nuevo.getSnapshotInmutable());
        }

        if (nuevo.getScoreRiesgo() != null &&
                (existente.getScoreRiesgo() == null || nuevo.getScoreRiesgo() > existente.getScoreRiesgo())) {
            existente.setScoreRiesgo(nuevo.getScoreRiesgo());
            existente.setNivelSeveridad(nuevo.getNivelSeveridad());
        }

        if (nuevo.getFechaVencimientoSla() != null &&
                (existente.getFechaVencimientoSla() == null ||
                        nuevo.getFechaVencimientoSla().isBefore(existente.getFechaVencimientoSla()))) {
            existente.setFechaVencimientoSla(nuevo.getFechaVencimientoSla());
        }

        log.info("Alerta consolidada exitosamente: {}", existente.getIdAlerta());
        return existente;
    }

    public boolean debeConsolidar(AlertaClinicaEntity nuevo) {
        if (nuevo == null || nuevo.getIdPersona() == null) return false;

        for (AlertaClinicaDetalleEntity detalle : nuevo.getDetalles()) {
            Optional<AlertaClinicaEntity> duplicado = encontrarDuplicado(
                    nuevo.getIdPersona(),
                    detalle.getTipoIndicador(),
                    nuevo.getFechaGeneracion()
            );
            if (duplicado.isPresent()) return true;
        }
        return false;
    }
}
