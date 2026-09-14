package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

public class MedicalSafetyViolationException extends RecomendacionesIAException {
    public MedicalSafetyViolationException(String mensaje) {
        super(mensaje, "MEDICAL_SAFETY_VIOLATION", "La recomendación generada viola reglas de seguridad médica");
    }
}