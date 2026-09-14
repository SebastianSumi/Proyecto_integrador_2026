package pe.edu.upeu.saludablemente.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return construirRespuesta(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        String mensaje = "Ruta no encontrada: /" + ex.getResourcePath();
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.NOT_FOUND.value(), mensaje);
        return construirRespuesta(HttpStatus.NOT_FOUND, "Not Found", mensaje);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.CONFLICT.value(), ex.getMessage());
        return construirRespuesta(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessConflict(BusinessConflictException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.CONFLICT.value(), ex.getMessage());
        return construirRespuesta(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(BusinessValidationException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(), "Error de validación en los datos enviados");
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Bad Request", "Error de validación en los datos enviados");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(), "Error de formato en los datos enviados");
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Bad Request", "Error de formato en los datos enviados");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(), "Error de formato en los parámetros enviados");
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Bad Request", "Error de formato en los parámetros enviados");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("api.error type={} status={} message={}", ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocurrió un error interno");
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status,
                                                                    String error,
                                                                    String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
