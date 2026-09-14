package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.WebhookPayloadDTO;

public interface WebhookDispatcherService {

    void despacharEvento(String tipoEvento, WebhookPayloadDTO payload);

    int contarSuscriptoresActivos();
}
