package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.exception;

public class EvaluacionOrigenInvalidaException extends MotorEvaluacionBaseException {

    public EvaluacionOrigenInvalidaException(String mensaje) {
        super(
                "Evaluación origen inválida: " + mensaje,
                "EVALUACION_ORIGEN_INVALIDA",
                "La evaluación nutricional contiene datos inválidos o incompletos"
        );
    }

    public EvaluacionOrigenInvalidaException(String campo, String valor) {
        super(
                String.format("Campo %s con valor %s inválido", campo, valor),
                "EVALUACION_ORIGEN_INVALIDA",
                String.format("El campo %s tiene un valor no válido", campo)
        );
    }
}