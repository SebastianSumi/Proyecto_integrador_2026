package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

public class LlmInferenceException extends RecomendacionesIAException {
    public LlmInferenceException(String mensaje) {
        super(mensaje, "LLM_INFERENCE_ERROR", "Error en el motor de inferencia de IA");
    }
}