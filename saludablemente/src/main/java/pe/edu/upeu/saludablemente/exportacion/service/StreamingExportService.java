package pe.edu.upeu.saludablemente.exportacion.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface StreamingExportService {

    StreamingResponseBody servirArchivo(String rutaAlmacenamiento);

    StreamingResponseBody servirArchivoComprimido(String rutaAlmacenamiento);
}
