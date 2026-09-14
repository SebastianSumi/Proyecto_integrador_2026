package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.DescargaExpiradaException;
import pe.edu.upeu.saludablemente.exportacion.dto.PresignedUrlDTO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PresignedUrlGeneratorImpl implements PresignedUrlGenerator {

    private static final String ALGORITMO_HMAC = "HmacSHA256";

    @Value("${exportacion.presigned.expiracion-minutos:15}")
    private long expiracionMinutos;

    @Value("${exportacion.presigned.secreto:${EXPORTACION_PRESIGNED_SECRET:cambiar-en-produccion}}")
    private String secreto;

    private final Map<UUID, LocalDateTime> tokensEmitidos = new ConcurrentHashMap<>();

    @Override
    public PresignedUrlDTO generarUrl(UUID idTarea, Long idUsuario) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(expiracionMinutos);

        String payload = idTarea + "|" + idUsuario + "|" + expiracion;
        String token = calcularHmac(payload);

        tokensEmitidos.put(idTarea, expiracion);

        String url = "/api/v1/exportaciones/" + idTarea + "/descargar?token=" + token;

        log.debug("URL prefirmada generada para tarea {}: expira en {} minutos",
                idTarea, expiracionMinutos);

        return PresignedUrlDTO.builder()
                .url(url)
                .token(token)
                .fechaEmision(ahora)
                .fechaExpiracion(expiracion)
                .build();
    }

    @Override
    public void verificarVigencia(UUID idTarea) {
        LocalDateTime expiracion = tokensEmitidos.get(idTarea);
        if (expiracion == null) {
            return;
        }
        if (LocalDateTime.now().isAfter(expiracion)) {
            tokensEmitidos.remove(idTarea);
            throw new DescargaExpiradaException(
                    "El enlace de descarga ha expirado. Regenerelo desde el historial.");
        }
    }

    private String calcularHmac(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO_HMAC));
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (Exception e) {
            log.error("Error al calcular HMAC: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudo firmar la URL", e);
        }
    }
}
