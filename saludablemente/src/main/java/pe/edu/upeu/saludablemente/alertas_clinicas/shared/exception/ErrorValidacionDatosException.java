package pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception;

public class ErrorValidacionDatosException extends AlertasClinicasException {

    public ErrorValidacionDatosException(String campo, String valor, String motivo) {
        super(
                String.format("Error de validación en campo %s con valor %s: %s", campo, valor, motivo),
                "ERROR_VALIDACION_DATOS",
                String.format("El campo %s tiene un valor inválido: %s", campo, motivo)
        );
    }

    public ErrorValidacionDatosException(String mensaje) {
        super(mensaje, "ERROR_VALIDACION_DATOS", mensaje);
    }
}