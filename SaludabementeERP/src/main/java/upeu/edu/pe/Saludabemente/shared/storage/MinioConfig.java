package upeu.edu.pe.Saludabemente.shared.storage;

import io.minio.MinioClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración única de MinIO para todo el ERP.
 * Un solo bucket (ver {@code saludablemente.storage.bucket-name}) con
 * prefijos por dominio ({@code noticias/}, {@code avatares/}, ...).
 */
@Configuration
@Getter
public class MinioConfig {

    @Value("${saludablemente.storage.minio.endpoint}")
    private String endpoint;

    @Value("${saludablemente.storage.minio.access-key}")
    private String accessKey;

    @Value("${saludablemente.storage.minio.secret-key}")
    private String secretKey;

    @Value("${saludablemente.storage.minio.bucket-name}")
    private String bucketName;

    @Value("${saludablemente.storage.minio.public-url}")
    private String publicUrl;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
