package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.config.RsaKeyConfig;
import pe.edu.upeu.saludablemente.auditoria.entity.ManifiestoArchivadoFrioEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoManifiesto;
import pe.edu.upeu.saludablemente.auditoria.repository.ManifiestoArchivadoFrioRepository;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriptographicManifestBuilderImpl implements CriptographicManifestBuilder {

    private final ManifiestoArchivadoFrioRepository manifiestoRepository;
    private final HashLedgerService hashLedgerService;
    private final RsaKeyConfig rsaKeyConfig;

    @Override
    public ManifiestoArchivadoFrioEntity construirManifiesto(LocalDateTime inicio, LocalDateTime fin,
                                                             int totalRegistros, String rutaColdStorage,
                                                             long tamanoBytes, String responsable) {
        String payloadManifiesto = String.format("%s|%s|%d|%s|%d",
                inicio.toString(), fin.toString(), totalRegistros, rutaColdStorage, tamanoBytes);

        String hashManifiesto = hashLedgerService.calcularHashSha256(payloadManifiesto);
        String firmaRsa = firmarManifiesto(hashManifiesto);

        ManifiestoArchivadoFrioEntity manifiesto = ManifiestoArchivadoFrioEntity.builder()
                .rangoFechaInicio(inicio)
                .rangoFechaFin(fin)
                .totalRegistrosExportados(totalRegistros)
                .rutaColdStorage(rutaColdStorage)
                .tamanoArchivoBytes(tamanoBytes)
                .hashManifiesto(hashManifiesto)
                .firmaRsa(firmaRsa)
                .fechaExportacion(LocalDateTime.now())
                .responsablePurga(responsable != null ? responsable : "sistema-scheduler")
                .estado(EstadoManifiesto.GENERADO)
                .build();

        ManifiestoArchivadoFrioEntity guardado = manifiestoRepository.save(manifiesto);

        log.info("Manifiesto de archivado construido: {} ({} registros, hash: {}...)",
                guardado.getIdManifiesto(), totalRegistros,
                hashManifiesto.substring(0, 12));

        return guardado;
    }

    private String firmarManifiesto(String hashManifiesto) {
        try {
            if (rsaKeyConfig.getPrivateKey() == null) {
                log.warn("Clave RSA privada no disponible. Firma del manifiesto no generada");
                return null;
            }

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(rsaKeyConfig.getPrivateKey());
            signature.update(hashManifiesto.getBytes(StandardCharsets.UTF_8));
            byte[] firma = signature.sign();

            return Base64.getEncoder().encodeToString(firma);

        } catch (Exception e) {
            log.error("Error al firmar manifiesto: {}", e.getMessage(), e);
            return null;
        }
    }
}
