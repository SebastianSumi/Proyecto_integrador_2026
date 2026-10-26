package upeu.edu.pe.Saludabemente.shared.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

import java.io.InputStream;
import java.util.UUID;

/**
 * Acceso único a MinIO. Un solo bucket con prefijos por dominio
 * ({@code noticias/}, {@code avatares/}, ...). Devuelve siempre el
 * nombre del objeto (con prefijo); la URL pública se construye aparte.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public String guardar(MultipartFile archivo) {
        return guardar(archivo, "");
    }

    public String guardar(MultipartFile archivo, String prefijo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessRuleException("El archivo es obligatorio");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
        }
        String base = (prefijo == null ? "" : prefijo.trim()).replaceAll("^/+|/+$", "");
        String objetoNombre = (base.isEmpty() ? "" : base + "/") + UUID.randomUUID() + extension;

        try (InputStream inputStream = archivo.getInputStream()) {
            asegurarBucketExiste();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objetoNombre)
                    .stream(inputStream, archivo.getSize(), -1)
                    .contentType(archivo.getContentType())
                    .build());
            log.info("Archivo subido a MinIO: {}", objetoNombre);
            return objetoNombre;
        } catch (Exception e) {
            log.error("Error al subir archivo a MinIO", e);
            throw new BusinessRuleException("No se pudo almacenar el archivo en MinIO: " + e.getMessage());
        }
    }

    public void eliminar(String objetoNombre) {
        if (objetoNombre == null || objetoNombre.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objetoNombre)
                    .build());
            log.info("Archivo eliminado de MinIO: {}", objetoNombre);
        } catch (Exception e) {
            log.warn("No se pudo eliminar archivo de MinIO: {}", objetoNombre, e);
        }
    }

    public String construirUrlPublica(String objetoNombre) {
        if (objetoNombre == null || objetoNombre.isBlank()) {
            return null;
        }
        String base = minioConfig.getPublicUrl().replaceAll("/+$", "");
        String objeto = objetoNombre.replaceAll("^/+", "");
        return base + "/" + minioConfig.getBucketName() + "/" + objeto;
    }

    private void asegurarBucketExiste() throws Exception {
        boolean existe = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .build());
        if (!existe) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            log.info("Bucket creado en MinIO: {}", minioConfig.getBucketName());
        }
        aplicarPoliticaPublica();
    }

    private void aplicarPoliticaPublica() {
        try {
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(minioConfig.getBucketName());

            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .config(policy)
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo aplicar política pública al bucket: {}", e.getMessage());
        }
    }
}
