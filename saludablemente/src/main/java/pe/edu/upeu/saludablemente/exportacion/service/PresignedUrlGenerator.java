package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.PresignedUrlDTO;

import java.util.UUID;

public interface PresignedUrlGenerator {

    PresignedUrlDTO generarUrl(UUID idTarea, Long idUsuario);

    void verificarVigencia(UUID idTarea);
}
