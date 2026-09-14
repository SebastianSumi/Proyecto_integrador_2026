package pe.edu.upeu.saludablemente.exportacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.saludablemente.exportacion.dto.WebhookPayloadDTO;
import pe.edu.upeu.saludablemente.exportacion.entity.SuscriptorWebhookEntity;
import pe.edu.upeu.saludablemente.exportacion.repository.SuscriptorWebhookRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatcherServiceImpl implements WebhookDispatcherService {

    private static final int MAX_REINTENTOS = 3;
    private static final long BACKOFF_INICIAL_MS = 1000L;

    private final SuscriptorWebhookRepository suscriptorRepository;
    private final WebhookPayloadSigner signer;
    private final ObjectMapper objectMapper;

    @Override
    public void despacharEvento(String tipoEvento, WebhookPayloadDTO payload) {
        List<SuscriptorWebhookEntity> suscriptores = suscriptorRepository.findByActivo("S");

        if (suscriptores.isEmpty()) {
            log.debug("No hay suscriptores activos para evento {}", tipoEvento);
            return;
        }

        log.info("Despachando evento {} a {} suscriptores", tipoEvento, suscriptores.size());

        for (SuscriptorWebhookEntity suscriptor : suscriptores) {
            despacharConReintentos(suscriptor, tipoEvento, payload);
        }
    }

    @Override
    public int contarSuscriptoresActivos() {
        return suscriptorRepository.findByActivo("S").size();
    }

    private void despacharConReintentos(SuscriptorWebhookEntity suscriptor,
                                        String tipoEvento, WebhookPayloadDTO payload) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error al serializar payload para suscriptor {}: {}",
                    suscriptor.getNombreSistema(), e.getMessage());
            return;
        }

        String firma = signer.firmar(payloadJson, suscriptor.getSecretoHmac());
        long backoff = BACKOFF_INICIAL_MS;

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                RestClient restClient = RestClient.builder()
                        .baseUrl(suscriptor.getUrlEndpoint())
                        .build();

                restClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", firma)
                        .header("X-Event-Type", tipoEvento)
                        .header("X-Suscriptor", suscriptor.getNombreSistema())
                        .body(payloadJson)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Webhook enviado exitosamente a {} en intento {}",
                        suscriptor.getNombreSistema(), intento);
                return;

            } catch (Exception e) {
                log.warn("Fallo webhook a {} (intento {}/{}): {}",
                        suscriptor.getNombreSistema(), intento, MAX_REINTENTOS, e.getMessage());

                if (intento == MAX_REINTENTOS) {
                    log.error("Webhook a {} fallo tras {} intentos.", suscriptor.getNombreSistema(), MAX_REINTENTOS);
                    return;
                }

                try {
                    Thread.sleep(backoff);
                    backoff *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
