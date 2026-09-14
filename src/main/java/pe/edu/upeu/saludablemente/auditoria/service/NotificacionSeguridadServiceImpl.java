package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionSeguridadServiceImpl implements NotificacionSeguridadService {

    @Override
    public void notificarAlertaCritica(AlertaSeguridadEntity alerta) {
        if (alerta == null) {
            return;
        }
        log.error("ALERTA CRITICA DE SEGURIDAD [{}] Usuario: {} - {}",
                alerta.getTipoAnomalia(),
                alerta.getUsuarioAfectado(),
                alerta.getMetadataContexto());
    }

    @Override
    public void notificarAlertaWarning(AlertaSeguridadEntity alerta) {
        if (alerta == null) {
            return;
        }
        log.warn("ALERTA DE SEGURIDAD [{}] Usuario: {}",
                alerta.getTipoAnomalia(),
                alerta.getUsuarioAfectado());
    }
}
