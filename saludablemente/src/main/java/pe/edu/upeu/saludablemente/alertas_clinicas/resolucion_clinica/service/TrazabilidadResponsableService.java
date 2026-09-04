package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TrazabilidadResponsableService {

    /**
     * Sella la resolución con la información del evaluador
     */
    public ResolucionClinicaEntity sellarConTrazabilidad(
            ResolucionClinicaEntity resolucion,
            Long idUsuarioEvaluador,
            String nombreEvaluador) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        if (idUsuarioEvaluador == null) {
            throw new IllegalArgumentException("El ID del evaluador es obligatorio");
        }

        resolucion.setIdUsuarioEvaluador(idUsuarioEvaluador);
        resolucion.setNombreEvaluador(nombreEvaluador);
        resolucion.setFechaAtencion(LocalDateTime.now());

        log.info("🔒 Resolución sellada por evaluador: {} (ID: {})",
                nombreEvaluador, idUsuarioEvaluador);

        return resolucion;
    }

    /**
     * Obtiene el timestamp actual del servidor
     */
    public LocalDateTime getTimestampActual() {
        return LocalDateTime.now();
    }

    /**
     * Valida que el evaluador tenga permisos para resolver la alerta
     */
    public boolean validarPermisosEvaluador(Long idEvaluador, String especialidadRequerida) {
        // TODO: Implementar validación con servicio de usuarios
        // Por ahora, todos tienen permisos
        return true;
    }
}