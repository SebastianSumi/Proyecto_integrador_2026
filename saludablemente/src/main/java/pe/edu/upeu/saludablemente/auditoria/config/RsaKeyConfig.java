package pe.edu.upeu.saludablemente.auditoria.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
@Getter
@Configuration
public class RsaKeyConfig {

    private static final int RSA_KEY_SIZE = 2048;

    @Value("${auditoria.rsa.private-key-path:./keys/auditoria-private.key}")
    private String privateKeyPathConfig;

    @Value("${auditoria.rsa.public-key-path:./keys/auditoria-public.key}")
    private String publicKeyPathConfig;

    @Value("${auditoria.rsa.auto-generate:true}")
    private boolean autoGenerate;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Path privateKeyPathAbsoluta;
    private Path publicKeyPathAbsoluta;

    @PostConstruct
    public void inicializarClaves() {
        try {
            this.privateKeyPathAbsoluta = resolverRutaAbsoluta(privateKeyPathConfig);
            this.publicKeyPathAbsoluta = resolverRutaAbsoluta(publicKeyPathConfig);

            if (Files.exists(privateKeyPathAbsoluta) && Files.exists(publicKeyPathAbsoluta)) {
                cargarClavesExistentes();
                log.info("Claves RSA de auditoria cargadas desde: {}", privateKeyPathAbsoluta.getParent());
            } else if (autoGenerate) {
                generarYPersistirClaves();
                log.info("Claves RSA de auditoria generadas en: {}", privateKeyPathAbsoluta.getParent());
            } else {
                throw new IllegalStateException(
                        "No existen claves RSA en " + privateKeyPathAbsoluta
                                + " y la autogeneracion esta deshabilitada");
            }
        } catch (Exception e) {
            log.error("Error al inicializar claves RSA: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudieron inicializar las claves RSA de auditoria", e);
        }
    }

    private void cargarClavesExistentes() throws Exception {
        byte[] privateBytes = Files.readAllBytes(privateKeyPathAbsoluta);
        byte[] publicBytes = Files.readAllBytes(publicKeyPathAbsoluta);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    private void generarYPersistirClaves() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        Files.createDirectories(privateKeyPathAbsoluta.getParent());
        Files.createDirectories(publicKeyPathAbsoluta.getParent());

        Files.write(privateKeyPathAbsoluta, privateKey.getEncoded());
        Files.write(publicKeyPathAbsoluta, publicKey.getEncoded());
    }

    private Path resolverRutaAbsoluta(String rutaConfigurada) {
        Path path = Paths.get(rutaConfigurada);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
