package pe.edu.upeu.saludablemente.exportacion.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "exportacion.storage.tipo", havingValue = "S3")
public class S3ExportStorageAdapter implements ExportStorageAdapter {

    @Override
    public String guardarArchivo(String rutaRelativa, byte[] contenido) {
        throw new UnsupportedOperationException(
                "Implementacion S3 pendiente. Usar tipo=LOCAL por ahora.");
    }

    @Override
    public byte[] obtenerArchivo(String rutaRelativa) {
        throw new UnsupportedOperationException("Implementacion S3 pendiente.");
    }

    @Override
    public boolean existeArchivo(String rutaRelativa) {
        throw new UnsupportedOperationException("Implementacion S3 pendiente.");
    }

    @Override
    public boolean eliminarArchivo(String rutaRelativa) {
        throw new UnsupportedOperationException("Implementacion S3 pendiente.");
    }

    @Override
    public long obtenerTamanoArchivo(String rutaRelativa) {
        throw new UnsupportedOperationException("Implementacion S3 pendiente.");
    }

    @Override
    public String obtenerTipoAlmacenamiento() {
        return "S3";
    }
}
