package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.ManifiestoArchivadoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ArchivadoFrioService {

    List<ManifiestoArchivadoDTO> archivarParticionesVencidas(String tabla);

    ManifiestoArchivadoDTO archivarParticion(String tabla, String nombreParticion,
                                             LocalDateTime inicio, LocalDateTime fin);
}
