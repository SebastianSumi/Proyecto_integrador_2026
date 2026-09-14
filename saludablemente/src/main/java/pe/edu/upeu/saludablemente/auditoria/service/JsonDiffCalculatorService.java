package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.DiferencialJsonDTO;

import java.util.Map;

public interface JsonDiffCalculatorService {

    DiferencialJsonDTO calcularDiferencial(Map<String, Object> estadoAnterior,
                                           Map<String, Object> estadoNuevo);

    String serializarDiferencial(DiferencialJsonDTO diferencial);

    String serializarMapa(Map<String, Object> mapa);
}
