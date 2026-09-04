package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

public class ConsolidacionDuplicadoException extends CicloVidaAlertaException {

    public ConsolidacionDuplicadoException(Long idPersona, TipoIndicador tipoIndicador) {
        super(
                String.format("Error al consolidar alertas duplicadas para persona %d, indicador %s",
                        idPersona, tipoIndicador),
                "ERROR_CONSOLIDACION_DUPLICADO",
                "No se pudo consolidar las alertas duplicadas"
        );
    }
}