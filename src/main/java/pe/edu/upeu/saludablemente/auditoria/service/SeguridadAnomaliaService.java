package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.detector.AnomaliaDetector;
import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;

import java.util.List;

public interface SeguridadAnomaliaService {

    void evaluarContexto(AnomaliaDetector.ContextoDeteccion contexto);

    void evaluarTextoParaInyeccion(String usuario, String texto, Long idPersona, String campo);

    List<AlertaSeguridadEntity> obtenerAlertasNoResueltas();
}
