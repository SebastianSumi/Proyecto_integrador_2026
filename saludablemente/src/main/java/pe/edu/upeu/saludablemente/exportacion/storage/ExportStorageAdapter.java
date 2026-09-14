package pe.edu.upeu.saludablemente.exportacion.storage;

public interface ExportStorageAdapter {

    String guardarArchivo(String rutaRelativa, byte[] contenido);

    byte[] obtenerArchivo(String rutaRelativa);

    boolean existeArchivo(String rutaRelativa);

    boolean eliminarArchivo(String rutaRelativa);

    long obtenerTamanoArchivo(String rutaRelativa);

    String obtenerTipoAlmacenamiento();
}
