package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.CifradoArchivoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AesZipEncryptor {

    private static final int AES_KEY_SIZE_BITS = 256;
    private static final Pattern PATRON_CONTRASENA = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{12,}$");

    public byte[] cifrarZip(byte[] zipPlano, String contrasena) {
        validarContrasena(contrasena);
        log.info("Aplicando cifrado AES-256 a archivo de {} bytes", zipPlano.length);

        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, 65536, AES_KEY_SIZE_BITS);
            SecretKey key = factory.generateSecret(spec);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] cifrado = cipher.doFinal(zipPlano);

            ByteArrayOutputStream resultado = new ByteArrayOutputStream();
            resultado.write(salt);
            resultado.write(iv);
            resultado.write(cifrado);
            return resultado.toByteArray();

        } catch (Exception e) {
            log.error("Error al cifrar con AES: {}", e.getMessage(), e);
            throw new CifradoArchivoException("Error al cifrar archivo ZIP", e);
        }
    }

    public void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new CifradoArchivoException("La contrasena de cifrado es obligatoria");
        }

        if (!PATRON_CONTRASENA.matcher(contrasena).matches()) {
            throw new CifradoArchivoException(
                    "La contrasena debe tener al menos 12 caracteres, una mayuscula, una minuscula, un numero y un simbolo");
        }
    }
}
