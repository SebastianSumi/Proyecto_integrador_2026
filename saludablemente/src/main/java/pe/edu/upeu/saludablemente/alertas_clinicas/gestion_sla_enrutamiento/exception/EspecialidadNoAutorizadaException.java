package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service.EnrutamientoEspecialidadService;

public class EspecialidadNoAutorizadaException extends GestionSlaEnrutamientoException {

    public EspecialidadNoAutorizadaException(Long idEvaluador, EnrutamientoEspecialidadService.Especialidad especialidadRequerida) {
        super(
                String.format("Evaluador %d no autorizado para especialidad %s",
                        idEvaluador, especialidadRequerida.getDisplayName()),
                "ESPECIALIDAD_NO_AUTORIZADA",
                String.format("No tiene autorización para atender casos de %s",
                        especialidadRequerida.getDisplayName())
        );
    }
}