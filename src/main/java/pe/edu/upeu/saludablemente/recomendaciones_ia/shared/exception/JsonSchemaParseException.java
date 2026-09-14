package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

public class JsonSchemaParseException extends RecomendacionesIAException {
    public JsonSchemaParseException(String mensaje) {
        super(mensaje, "JSON_SCHEMA_PARSE_ERROR", "Error al parsear la respuesta del modelo IA");
    }
}