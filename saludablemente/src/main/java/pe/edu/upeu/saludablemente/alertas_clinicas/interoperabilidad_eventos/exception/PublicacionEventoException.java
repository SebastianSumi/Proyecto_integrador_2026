package pe.edu.upeu.saludablemente.alertas_clinicas.interoperabilidad_eventos.exception;

public class PublicacionEventoException extends InteroperabilidadEventosException {

    public PublicacionEventoException(String tipoEvento, Throwable cause) {
        super(
                String.format("Error al publicar evento %s", tipoEvento),
                "ERROR_PUBLICACION_EVENTO",
                "No se pudo notificar el evento correctamente"
        );
    }
}