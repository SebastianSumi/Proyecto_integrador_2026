package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;

public interface NotificacionSeguridadService {

    void notificarAlertaCritica(AlertaSeguridadEntity alerta);

    void notificarAlertaWarning(AlertaSeguridadEntity alerta);
}
