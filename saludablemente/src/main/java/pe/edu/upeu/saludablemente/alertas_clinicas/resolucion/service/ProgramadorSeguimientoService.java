package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.CatalogoAccionCorrectivaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramadorSeguimientoService {

    private static final int DIAS_SEGUIMIENTO_PREDETERMINADO = 30;

    /**
     * Programa el seguimiento clínico (30 o 60 días) según la acción correctiva
     */
    public ResolucionClinicaEntity programarSeguimiento(
            ResolucionClinicaEntity resolucion,
            CatalogoAccionCorrectivaEntity accionCorrectiva) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        if (accionCorrectiva == null || !accionCorrectiva.isRequiereSeguimiento()) {
            resolucion.setEsSeguimientoProgramado(false);
            resolucion.setFechaSeguimientoProgramado(null);
            resolucion.setDiasSeguimiento(null);
            return resolucion;
        }

        Integer diasSeguimiento = accionCorrectiva.getDiasSeguimiento();
        if (diasSeguimiento == null || diasSeguimiento <= 0) {
            diasSeguimiento = DIAS_SEGUIMIENTO_PREDETERMINADO;
        }

        LocalDateTime fechaSeguimiento = LocalDateTime.now().plusDays(diasSeguimiento);

        resolucion.setEsSeguimientoProgramado(true);
        resolucion.setFechaSeguimientoProgramado(fechaSeguimiento);
        resolucion.setDiasSeguimiento(diasSeguimiento);

        log.info("Seguimiento programado para alerta en {} días (fecha: {})", diasSeguimiento, fechaSeguimiento);
        return resolucion;
    }

    public boolean tieneSeguimientoPendiente(ResolucionClinicaEntity resolucion) {
        return resolucion != null &&
                resolucion.isSeguimientoProgramado() &&
                resolucion.getFechaSeguimientoProgramado() != null &&
                resolucion.getFechaSeguimientoProgramado().isAfter(LocalDateTime.now());
    }
}
