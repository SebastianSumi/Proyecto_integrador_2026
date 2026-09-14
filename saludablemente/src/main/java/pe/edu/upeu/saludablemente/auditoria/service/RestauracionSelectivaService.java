package pe.edu.upeu.saludablemente.auditoria.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RestauracionSelectivaService {

    List<Map<String, Object>> restaurarRango(LocalDateTime inicio, LocalDateTime fin);

    byte[] descargarArchivoArchivado(String rutaColdStorage);

    boolean verificarIntegridadArchivo(String rutaColdStorage, String hashEsperado);
}
