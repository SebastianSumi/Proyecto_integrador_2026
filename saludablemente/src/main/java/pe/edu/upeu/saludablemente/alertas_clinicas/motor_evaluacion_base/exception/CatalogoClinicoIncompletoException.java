package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.exception;

public class CatalogoClinicoIncompletoException extends MotorEvaluacionBaseException {

    public CatalogoClinicoIncompletoException(String tipoCatalogo, String descripcion) {
        super(
                String.format("Catálogo clínico incompleto: %s - %s", tipoCatalogo, descripcion),
                "CATALOGO_CLINICO_INCOMPLETO",
                "La referencia clínica no está disponible para el cálculo solicitado"
        );
    }
}