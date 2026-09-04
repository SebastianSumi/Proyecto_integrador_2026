package pe.edu.upeu.saludablemente.alertas_clinicas.scoring_inteligente.exception;

public class SindromeNoDetectadoException extends ScoringInteligenteException {

    public SindromeNoDetectadoException() {
        super(
                "No se detectaron síndromes clínicos en los indicadores evaluados",
                "SINDROME_NO_DETECTADO",
                "No se encontró un patrón sindrómico en las alteraciones"
        );
    }
}
