package pe.edu.upeu.saludablemente.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.Instant;
import java.util.List;

import static pe.edu.upeu.saludablemente.filter.CorrelationIdFilter.TRACE_ID_MDC_KEY;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldViolation)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, violations);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiError.FieldViolation> violations
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                violations
        );
        return ResponseEntity.status(status).body(error);
    }

    private ApiError.FieldViolation toFieldViolation(FieldError fieldError) {
        return new ApiError.FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
