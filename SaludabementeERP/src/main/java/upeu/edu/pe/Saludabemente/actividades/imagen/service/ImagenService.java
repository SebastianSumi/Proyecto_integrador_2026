package upeu.edu.pe.Saludabemente.actividades.imagen.service;

import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;

import java.util.List;

public interface ImagenService {

    /** Sube o reemplaza el logo. Elimina el anterior de MinIO si existia. */
    String subirLogo(Long idActividad, MultipartFile archivo);

    /** Elimina el logo actual. */
    void eliminarLogo(Long idActividad);

    /** Agrega una imagen a la galeria. */
    ImagenResponse subirImagen(Long idActividad, MultipartFile archivo, String descripcion);

    /** Lista imagenes de la galeria. */
    List<ImagenResponse> listarImagenes(Long idActividad);

    /** Elimina una imagen de la galeria (MinIO + BD). */
    void eliminarImagen(Long idActividad, Long idImagen);
}