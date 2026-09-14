package pe.edu.upeu.saludablemente.exportacion.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.StorageExportException;
import pe.edu.upeu.saludablemente.exportacion.config.ExportStorageConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "exportacion.storage.tipo",
        havingValue = "LOCAL", matchIfMissing = true)
public class LocalExportStorageAdapter implements ExportStorageAdapter {

    private final ExportStorageConfig storageConfig;

    @Override
    public String guardarArchivo(String rutaRelativa, byte[] contenido) {
        try {
            Path rutaCompleta = storageConfig.getRutaFinales().resolve(rutaRelativa).normalize();
            Files.createDirectories(rutaCompleta.getParent());
            Files.write(rutaCompleta, contenido,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Archivo guardado: {} ({} bytes)", rutaCompleta, contenido.length);
            return rutaRelativa;
        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage(), e);
            throw new StorageExportException("No se pudo guardar el archivo en storage", e);
        }
    }

    @Override
    public byte[] obtenerArchivo(String rutaRelativa) {
        try {
            Path rutaCompleta = storageConfig.getRutaFinales().resolve(rutaRelativa).normalize();
            if (!Files.exists(rutaCompleta)) {
                throw new StorageExportException("El archivo no existe: " + rutaRelativa);
            }
            return Files.readAllBytes(rutaCompleta);
        } catch (IOException e) {
            log.error("Error al leer archivo: {}", e.getMessage(), e);
            throw new StorageExportException("No se pudo recuperar el archivo", e);
        }
    }

    @Override
    public boolean existeArchivo(String rutaRelativa) {
        return Files.exists(storageConfig.getRutaFinales().resolve(rutaRelativa).normalize());
    }

    @Override
    public boolean eliminarArchivo(String rutaRelativa) {
        try {
            return Files.deleteIfExists(
                    storageConfig.getRutaFinales().resolve(rutaRelativa).normalize());
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long obtenerTamanoArchivo(String rutaRelativa) {
        try {
            return Files.size(storageConfig.getRutaFinales().resolve(rutaRelativa).normalize());
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public String obtenerTipoAlmacenamiento() {
        return "LOCAL";
    }
}
