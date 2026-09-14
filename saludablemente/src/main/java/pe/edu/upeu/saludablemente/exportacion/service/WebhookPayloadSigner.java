package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Component
public class WebhookPayloadSigner {

    private static final String ALGORITMO = "HmacSHA256";

    public String firmar(String payload, String secreto) {
        if (payload == null || secreto == null) {
            return "";
        }

        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            SecretKeySpec keySpec = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Error al firmar payload webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error al firmar payload con HMAC-SHA256", e);
        }
    }

    public boolean verificarFirma(String payload, String firmaRecibida, String secreto) {
        String firmaEsperada = firmar(payload, secreto);
        return firmaEsperada.equals(firmaRecibida);
    }
}
