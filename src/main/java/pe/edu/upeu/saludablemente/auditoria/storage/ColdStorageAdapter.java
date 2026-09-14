package pe.edu.upeu.saludablemente.auditoria.storage;

public interface ColdStorageAdapter {

    String guardarArchivo(String rutaRelativa, byte[] contenido);

    byte[] obtenerArchivo(String rutaRelativa);

    boolean existeArchivo(String rutaRelativa);

    boolean eliminarArchivo(String rutaRelativa);

    long obtenerTamanoArchivo(String rutaRelativa);

    String obtenerTipoAlmacenamiento();
}
