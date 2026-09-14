package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception.RecomendacionesIAException;

public class RecomendacionNoVigenteException extends RecomendacionesIAException {
    public RecomendacionNoVigenteException(String mensaje) {
        super(mensaje, "RECOMENDACION_NO_VIGENTE", mensaje);
    }
}