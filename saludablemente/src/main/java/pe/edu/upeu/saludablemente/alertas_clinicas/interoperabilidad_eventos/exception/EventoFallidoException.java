package pe.edu.upeu.saludablemente.alertas_clinicas.interoperabilidad_eventos.exception;

public class EventoFallidoException extends InteroperabilidadEventosException {

    public EventoFallidoException(String tipoEvento, String motivo) {
        super(
                String.format("Evento %s falló: %s", tipoEvento, motivo),
                "EVENTO_FALLIDO",
                "No se pudo procesar el evento correctamente"
        );
    }
}