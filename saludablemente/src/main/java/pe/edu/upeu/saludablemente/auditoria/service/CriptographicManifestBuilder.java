package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.entity.ManifiestoArchivadoFrioEntity;

import java.time.LocalDateTime;

public interface CriptographicManifestBuilder {

    ManifiestoArchivadoFrioEntity construirManifiesto(
            LocalDateTime inicio, LocalDateTime fin,
            int totalRegistros, String rutaColdStorage,
            long tamanoBytes, String responsable);
}
