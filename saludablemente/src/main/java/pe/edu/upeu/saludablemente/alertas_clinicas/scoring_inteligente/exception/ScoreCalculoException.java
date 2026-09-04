package pe.edu.upeu.saludablemente.alertas_clinicas.scoring_inteligente.exception;

public class ScoreCalculoException extends ScoringInteligenteException {

    public ScoreCalculoException(String motivo) {
        super(
                "Error al calcular el score de severidad: " + motivo,
                "ERROR_CALCULO_SCORE",
                "No se pudo determinar la severidad de la alerta"
        );
    }
}