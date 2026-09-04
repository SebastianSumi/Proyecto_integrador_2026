package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.exception;

public class CalculoIndicadorException extends MotorEvaluacionBaseException {

    public CalculoIndicadorException(String indicador, String motivo) {
        super(
                String.format("Error al calcular %s: %s", indicador, motivo),
                "ERROR_CALCULO_INDICADOR",
                String.format("No se pudo calcular el indicador %s", indicador)
        );
    }
}