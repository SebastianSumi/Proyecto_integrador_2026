package upeu.edu.pe.Saludabemente.shared.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

/**
 * Validación y delegación de imágenes hacia MinIO.
 * El llamante indica el prefijo del dominio ({@code noticias}, {@code avatares}, ...).
 */
@Service
@RequiredArgsConstructor
public class AlmacenamientoImagenService {

    private static final long MAX_BYTES_DEFAULT = 10L * 1024 * 1024;

    private final MinioService minioService;

    public String guardar(MultipartFile archivo) {
        return guardar(archivo, "", MAX_BYTES_DEFAULT);
    }

    public String guardar(MultipartFile archivo, String prefijo) {
        return guardar(archivo, prefijo, MAX_BYTES_DEFAULT);
    }

    public String guardar(MultipartFile archivo, String prefijo, long maxBytes) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessRuleException("La imagen es obligatoria");
        }
        if (archivo.getSize() > maxBytes) {
            throw new BusinessRuleException(
                    "La imagen supera el límite de " + (maxBytes / 1024 / 1024) + " MB");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessRuleException("El archivo no es una imagen válida");
        }
        return minioService.guardar(archivo, prefijo);
    }

    public void eliminar(String objetoNombre) {
        minioService.eliminar(objetoNombre);
    }

    public String construirUrlPublica(String objetoNombre) {
        return minioService.construirUrlPublica(objetoNombre);
    }
}
