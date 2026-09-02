package pe.edu.upeu.saludablemente.web.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message) {
    }
}