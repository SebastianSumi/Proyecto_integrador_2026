package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

public class ContextoClinicoIncompletoException extends RecomendacionesIAException {
    public ContextoClinicoIncompletoException(String mensaje) {
        super(mensaje, "CONTEXTO_CLINICO_INCOMPLETO", "Faltan datos clínicos necesarios para generar la recomendación");
    }
}