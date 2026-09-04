package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.CatalogoAccionCorrectivaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramadorSeguimientoService {

    private static final int DIAS_SEGUIMIENTO_PREDETERMINADO = 30;

    /**
     * Programa el seguimiento para una resolución
     */
    public ResolucionClinicaEntity programarSeguimiento(
            ResolucionClinicaEntity resolucion,
            CatalogoAccionCorrectivaEntity accionCorrectiva) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        if (accionCorrectiva == null || !accionCorrectiva.isRequiereSeguimiento()) {
            resolucion.setEsSeguimientoProgramado(false);
            return resolucion;
        }

        int diasSeguimiento = accionCorrectiva.getDiasSeguimiento() != null ?
                accionCorrectiva.getDiasSeguimiento() : DIAS_SEGUIMIENTO_PREDETERMINADO;

        LocalDateTime fechaSeguimiento = LocalDateTime.now().plusDays(diasSeguimiento);

        resolucion.setEsSeguimientoProgramado(true);
        resolucion.setFechaSeguimientoProgramado(fechaSeguimiento);

        log.info("📅 Seguimiento programado para alerta {} en {} días (fecha: {})",
                resolucion.getAlerta().getIdAlerta(),
                diasSeguimiento,
                fechaSeguimiento);

        return resolucion;
    }

    /**
     * Verifica si una resolución requiere seguimiento
     */
    public boolean requiereSeguimiento(CatalogoAccionCorrectivaEntity accionCorrectiva) {
        return accionCorrectiva != null && accionCorrectiva.isRequiereSeguimiento();
    }

    /**
     * Obtiene los días de seguimiento
     */
    public int getDiasSeguimiento(CatalogoAccionCorrectivaEntity accionCorrectiva) {
        if (accionCorrectiva == null || !accionCorrectiva.isRequiereSeguimiento()) {
            return 0;
        }
        return accionCorrectiva.getDiasSeguimiento() != null ?
                accionCorrectiva.getDiasSeguimiento() : DIAS_SEGUIMIENTO_PREDETERMINADO;
    }

    /**
     * Genera una alerta de seguimiento (evento)
     * Este método será llamado por el orquestador para disparar el evento
     */
    public void generarAlertaSeguimiento(ResolucionClinicaEntity resolucion) {
        if (resolucion == null || !Boolean.TRUE.equals(resolucion.getEsSeguimientoProgramado())) {
            return;
        }

        log.info("🔔 Generando alerta de seguimiento para resolución: {}",
                resolucion.getIdResolucion());

        // TODO: Publicar evento de seguimiento
        // Esto se implementará en el submódulo de Interoperabilidad_Eventos
    }
}