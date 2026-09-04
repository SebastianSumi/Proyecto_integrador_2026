package pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception;

public class RecursoNoEncontradoException extends AlertasClinicasException {

    public RecursoNoEncontradoException(String recurso, String identificador) {
        super(
                String.format("Recurso %s con identificador %s no encontrado", recurso, identificador),
                "RECURSO_NO_ENCONTRADO",
                String.format("No se encontró el %s solicitado", recurso)
        );
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje, "RECURSO_NO_ENCONTRADO", mensaje);
    }
}