package pe.edu.upeu.saludablemente.perfil_reporte.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
@ConditionalOnProperty(name = "perfil.reporte.storage.tipo", havingValue = "LOCAL", matchIfMissing = true)
public class LocalStorageAdapter implements StorageAdapter {

    @Value("${perfil.reporte.storage.ruta-base:./perfil-reporte-storage}")
    private String rutaBaseConfig;

    private Path rutaBaseAbsoluta;

    @PostConstruct
    public void inicializar() {
        this.rutaBaseAbsoluta = resolverRutaAbsoluta(rutaBaseConfig);
        try {
            Files.createDirectories(rutaBaseAbsoluta);
            Files.createDirectories(rutaBaseAbsoluta.resolve("temporal"));
            Files.createDirectories(rutaBaseAbsoluta.resolve("finales"));
            log.info("Storage de Perfil / Reporte inicializado en: {}", rutaBaseAbsoluta);
        } catch (IOException e) {
            log.error("No se pudo crear el directorio base de reportes: {}", rutaBaseAbsoluta, e);
            throw new BusinessException("No se pudo inicializar el storage de reportes");
        }
    }

    @Override
    public String guardarArchivo(String rutaRelativa, byte[] contenido) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            Files.createDirectories(rutaCompleta.getParent());
            Files.write(rutaCompleta, contenido,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Archivo de reporte guardado: {} ({} bytes)", rutaCompleta, contenido.length);
            return rutaRelativa;
        } catch (IOException e) {
            log.error("Error al guardar archivo de reporte: {}", e.getMessage(), e);
            throw new BusinessException("Error al guardar el archivo de reporte");
        }
    }

    @Override
    public void guardarArchivoDesdeStream(String rutaRelativa, InputStream inputStream) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            Files.createDirectories(rutaCompleta.getParent());
            Files.copy(inputStream, rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al guardar archivo desde stream: {}", e.getMessage(), e);
            throw new BusinessException("Error al guardar el archivo de reporte");
        }
    }

    @Override
    public byte[] obtenerArchivo(String rutaRelativa) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            if (!Files.exists(rutaCompleta)) {
                throw new BusinessException("El archivo no existe: " + rutaRelativa);
            }
            return Files.readAllBytes(rutaCompleta);
        } catch (IOException e) {
            throw new BusinessException("Error al leer el archivo de reporte");
        }
    }

    @Override
    public InputStream obtenerArchivoComoStream(String rutaRelativa) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            if (!Files.exists(rutaCompleta)) {
                throw new BusinessException("El archivo no existe: " + rutaRelativa);
            }
            return Files.newInputStream(rutaCompleta);
        } catch (IOException e) {
            throw new BusinessException("Error al leer el archivo de reporte");
        }
    }

    @Override
    public void copiarAStream(String rutaRelativa, OutputStream outputStream) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            if (!Files.exists(rutaCompleta)) {
                throw new BusinessException("El archivo no existe: " + rutaRelativa);
            }
            Files.copy(rutaCompleta, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new BusinessException("Error al copiar el archivo de reporte");
        }
    }

    @Override
    public boolean existeArchivo(String rutaRelativa) {
        return Files.exists(rutaBaseAbsoluta.resolve(rutaRelativa).normalize());
    }

    @Override
    public boolean eliminarArchivo(String rutaRelativa) {
        try {
            return Files.deleteIfExists(rutaBaseAbsoluta.resolve(rutaRelativa).normalize());
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long obtenerTamanoArchivo(String rutaRelativa) {
        try {
            return Files.size(rutaBaseAbsoluta.resolve(rutaRelativa).normalize());
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public Path obtenerRutaFisica(String rutaRelativa) {
        return rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
    }

    @Override
    public String obtenerTipoAlmacenamiento() {
        return "LOCAL";
    }

    private Path resolverRutaAbsoluta(String rutaConfigurada) {
        Path path = Paths.get(rutaConfigurada);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
