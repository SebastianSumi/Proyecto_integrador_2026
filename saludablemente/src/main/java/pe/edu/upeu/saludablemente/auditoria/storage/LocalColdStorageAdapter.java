package pe.edu.upeu.saludablemente.auditoria.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ColdStorageOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
@ConditionalOnProperty(name = "auditoria.cold-storage.tipo", havingValue = "LOCAL", matchIfMissing = true)
public class LocalColdStorageAdapter implements ColdStorageAdapter {

    @Value("${auditoria.cold-storage.ruta-base-local:./auditoria-cold-storage}")
    private String rutaBaseLocal;

    private Path rutaBaseAbsoluta;

    @PostConstruct
    public void inicializar() {
        this.rutaBaseAbsoluta = resolverRutaAbsoluta(rutaBaseLocal);
        try {
            Files.createDirectories(rutaBaseAbsoluta);
            log.info("Cold storage local inicializado en: {}", rutaBaseAbsoluta);
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de cold storage: {}", rutaBaseAbsoluta, e);
            throw new ColdStorageOperationException("Error al crear directorio base de cold storage: " + rutaBaseAbsoluta, e);
        }
    }

    @Override
    public String guardarArchivo(String rutaRelativa, byte[] contenido) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            Files.createDirectories(rutaCompleta.getParent());
            Files.write(rutaCompleta, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Archivo guardado en cold storage: {} ({} bytes)", rutaCompleta, contenido.length);
            return rutaRelativa;
        } catch (IOException e) {
            log.error("Error al guardar archivo en cold storage: {}", e.getMessage(), e);
            throw new ColdStorageOperationException("Error al guardar archivo en cold storage: " + rutaRelativa, e);
        }
    }

    @Override
    public byte[] obtenerArchivo(String rutaRelativa) {
        try {
            Path rutaCompleta = rutaBaseAbsoluta.resolve(rutaRelativa).normalize();
            if (!Files.exists(rutaCompleta)) {
                throw new ColdStorageOperationException("El archivo no existe en cold storage: " + rutaRelativa);
            }
            return Files.readAllBytes(rutaCompleta);
        } catch (IOException e) {
            log.error("Error al leer archivo de cold storage: {}", e.getMessage(), e);
            throw new ColdStorageOperationException("Error al leer archivo de cold storage: " + rutaRelativa, e);
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
            log.error("Error al eliminar archivo de cold storage: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long obtenerTamanoArchivo(String rutaRelativa) {
        try {
            return Files.size(rutaBaseAbsoluta.resolve(rutaRelativa).normalize());
        } catch (IOException e) {
            log.error("Error al obtener tamano del archivo: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public String obtenerTipoAlmacenamiento() {
        return "LOCAL";
    }

    public Path getRutaBaseAbsoluta() {
        return rutaBaseAbsoluta;
    }

    private Path resolverRutaAbsoluta(String rutaConfigurada) {
        if (rutaConfigurada == null || rutaConfigurada.isBlank()) {
            return Paths.get("./auditoria-cold-storage").toAbsolutePath().normalize();
        }
        Path path = Paths.get(rutaConfigurada);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
