package pe.edu.upeu.saludablemente.perfil_reporte.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.UUID;

public interface StreamingPdfService {

    StreamingResponseBody obtenerStreamingDescarga(UUID idReporte);

    void escribirPdfDirectoAlStream(UUID idReporte, OutputStream outputStream);

    long obtenerTamanoArchivo(UUID idReporte);

    String obtenerNombreArchivo(UUID idReporte);
}
