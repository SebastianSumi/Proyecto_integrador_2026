package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service.EnrutamientoEspecialidadService;

public class NoHayEvaluadoresDisponiblesException extends GestionSlaEnrutamientoException {

    public NoHayEvaluadoresDisponiblesException(EnrutamientoEspecialidadService.Especialidad especialidad) {
        super(
                String.format("No hay evaluadores disponibles para especialidad %s",
                        especialidad.getDisplayName()),
                "NO_HAY_EVALUADORES",
                String.format("No hay evaluadores disponibles para %s. Contacte al administrador",
                        especialidad.getDisplayName())
        );
    }
}