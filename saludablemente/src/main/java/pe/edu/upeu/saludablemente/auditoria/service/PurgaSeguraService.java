package pe.edu.upeu.saludablemente.auditoria.service;

import java.util.List;

public interface PurgaSeguraService {

    List<String> purgarParticionesVencidas(String tabla);

    void purgarParticion(String tabla, String nombreParticion);
}
