package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TrazabilidadResponsableService {

    /**
     * Sella la resolución con la información del evaluador y timestamp exacto
     */
    public ResolucionClinicaEntity sellarConTrazabilidad(
            ResolucionClinicaEntity resolucion,
            Long idUsuarioEvaluador,
            String nombreEvaluador) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        Long idEvaluadorFinal = idUsuarioEvaluador != null ? idUsuarioEvaluador : 1L;
        String nombreFinal = (nombreEvaluador != null && !nombreEvaluador.isBlank()) ? nombreEvaluador : "Evaluador " + idEvaluadorFinal;

        resolucion.setIdUsuarioEvaluador(idEvaluadorFinal);
        resolucion.setNombreEvaluador(nombreFinal);
        resolucion.setFechaAtencion(LocalDateTime.now());

        log.info("Resolución sellada por evaluador: {} (ID: {})", nombreFinal, idEvaluadorFinal);
        return resolucion;
    }

    public LocalDateTime getTimestampActual() {
        return LocalDateTime.now();
    }
}
