package pe.edu.upeu.saludablemente.perfil_reporte.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface StorageAdapter {

    String guardarArchivo(String rutaRelativa, byte[] contenido);

    void guardarArchivoDesdeStream(String rutaRelativa, InputStream inputStream);

    byte[] obtenerArchivo(String rutaRelativa);

    InputStream obtenerArchivoComoStream(String rutaRelativa);

    void copiarAStream(String rutaRelativa, OutputStream outputStream);

    boolean existeArchivo(String rutaRelativa);

    boolean eliminarArchivo(String rutaRelativa);

    long obtenerTamanoArchivo(String rutaRelativa);

    Path obtenerRutaFisica(String rutaRelativa);

    String obtenerTipoAlmacenamiento();
}
