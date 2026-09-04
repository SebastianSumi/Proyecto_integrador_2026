package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception;

public class AccionCorrectivaNoPermitidaException extends ResolucionClinicaException {

    public AccionCorrectivaNoPermitidaException(String accion, String motivo) {
        super(
                String.format("Acción correctiva no permitida: %s - %s", accion, motivo),
                "ACCION_NO_PERMITIDA",
                "La acción correctiva seleccionada no es válida para este caso"
        );
    }
}