package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pe.edu.upeu.saludablemente.exception.StorageExportException;
import pe.edu.upeu.saludablemente.exportacion.config.ExportStorageConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingExportServiceImpl implements StreamingExportService {

    private static final int BUFFER_SIZE = 8192;

    private final ExportStorageConfig storageConfig;

    @Override
    public StreamingResponseBody servirArchivo(String rutaAlmacenamiento) {
        return outputStream -> escribirArchivo(rutaAlmacenamiento, outputStream, false);
    }

    @Override
    public StreamingResponseBody servirArchivoComprimido(String rutaAlmacenamiento) {
        return outputStream -> escribirArchivo(rutaAlmacenamiento, outputStream, true);
    }

    private void escribirArchivo(String rutaAlmacenamiento,
                                  OutputStream outputStream,
                                  boolean comprimir) {
        Path ruta = storageConfig.getRutaFinales().resolve(rutaAlmacenamiento).normalize();

        if (!Files.exists(ruta)) {
            throw new StorageExportException("El archivo no existe: " + rutaAlmacenamiento);
        }

        try (InputStream input = Files.newInputStream(ruta)) {

            if (comprimir) {
                try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                    copiar(input, gzip);
                }
            } else {
                copiar(input, outputStream);
            }

            log.debug("Archivo servido: {} ({} bytes)",
                    rutaAlmacenamiento, Files.size(ruta));

        } catch (Exception e) {
            log.error("Error al servir archivo {}: {}", rutaAlmacenamiento, e.getMessage(), e);
            throw new StorageExportException("Error al servir el archivo", e);
        }
    }

    private void copiar(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesLeidos;
        while ((bytesLeidos = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesLeidos);
        }
        output.flush();
    }
}
