package pe.edu.upeu.saludablemente.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsUnexpectedErrorsToUniformInternalServerError() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(new RuntimeException("database details"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .containsEntry("status", 500)
                .containsEntry("error", "Internal Server Error")
                .containsEntry("message", "Ocurrió un error interno");
        assertThat(response.getBody()).containsKey("timestamp");
    }
}
