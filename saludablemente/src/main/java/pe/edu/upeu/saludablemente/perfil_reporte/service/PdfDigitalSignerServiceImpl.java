package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.ErrorGeneracionPdfException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Service
public class PdfDigitalSignerServiceImpl implements PdfDigitalSignerService {

    private static final String ALGORITMO = "SHA-256";

    @Override
    public String calcularHashSha256(byte[] contenido) {
        if (contenido == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] hash = digest.digest(contenido);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo SHA-256 no disponible", e);
            throw new ErrorGeneracionPdfException("Error al calcular hash SHA-256", e);
        }
    }

    @Override
    public String calcularHashSha256(String contenido) {
        if (contenido == null) {
            return "";
        }
        return calcularHashSha256(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generarFirmaDigital(String hashSha256, String idReporte) {
        return calcularHashSha256(hashSha256 + "|" + idReporte);
    }
}
