package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
public class PseudonymizationCryptoService {

    private static final String ALGORITMO_HMAC = "HmacSHA256";

    @Value("${exportacion.pseudonimizacion.salt:SALUDABLEMENTE_SALT_2026_SECRET}")
    private String salt;

    public String pseudonimizar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            SecretKeySpec keySpec = new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), ALGORITMO_HMAC);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(identificador.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Error al pseudonimizar identificador: {}", e.getMessage(), e);
            throw new RuntimeException("Error en pseudonimizacion criptografica", e);
        }
    }

    public List<CohorteClinicaDTO> pseudonimizarLote(List<CohorteClinicaDTO> cohortes) {
        log.info("Aplicando pseudonimizacion sobre {} colaboradores", cohortes.size());

        for (CohorteClinicaDTO c : cohortes) {
            if (c.getFiliacion() != null) {
                if (c.getFiliacion().getCodigoColaborador() != null) {
                    String pseudonimo = pseudonimizar(c.getFiliacion().getCodigoColaborador());
                    c.getFiliacion().setCodigoColaborador("PSD-" + pseudonimo.substring(0, 16));
                }
                c.getFiliacion().setNombreCompleto("Colaborador Pseudonimizado");
            }
        }
        return cohortes;
    }
}
