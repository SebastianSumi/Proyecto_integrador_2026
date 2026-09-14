package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.ParticionBitacoraEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.ParticionBitacoraRepository;
import pe.edu.upeu.saludablemente.exception.ParticionamientoException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurgaSeguraServiceImpl implements PurgaSeguraService {

    private final ParticionBitacoraRepository particionRepository;
    private final ParticionamientoOracleService particionamientoService;

    @Value("${auditoria.retencion.anios-retencion-legal:5}")
    private int aniosRetencionLegal;

    @Override
    @Transactional
    public List<String> purgarParticionesVencidas(String tabla) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusYears(aniosRetencionLegal);

        log.info("Buscando particiones de {} candidatas a purga (anteriores a {})", tabla, fechaLimite);

        List<ParticionBitacoraEntity> candidatas = particionRepository
                .findCandidatasPurga(tabla, fechaLimite);

        if (candidatas.isEmpty()) {
            log.info("No hay particiones candidatas a purga en {}", tabla);
            return List.of();
        }

        List<String> purgadas = new ArrayList<>();

        for (ParticionBitacoraEntity particion : candidatas) {
            if (!"S".equalsIgnoreCase(particion.getArchivada())) {
                log.warn("Particion {} no esta archivada. Se omite purga por seguridad",
                        particion.getNombreParticion());
                continue;
            }

            try {
                purgarParticion(tabla, particion.getNombreParticion());
                purgadas.add(particion.getNombreParticion());

            } catch (Exception e) {
                log.error("Error al purgar particion {}: {}",
                        particion.getNombreParticion(), e.getMessage(), e);
            }
        }

        return purgadas;
    }

    @Override
    @Transactional
    public void purgarParticion(String tabla, String nombreParticion) {
        log.info("Purgando particion {} de {}", nombreParticion, tabla);

        try {
            particionamientoService.eliminarParticion(tabla, nombreParticion);

            particionRepository.findByNombreParticion(nombreParticion).ifPresent(p -> {
                particionRepository.marcarComoPurgada(p.getIdParticion());
            });

            log.info("Particion {} purgada exitosamente", nombreParticion);

        } catch (Exception e) {
            log.error("Error al purgar particion {}: {}", nombreParticion, e.getMessage(), e);
            throw new ParticionamientoException("Error al purgar particion " + nombreParticion, e);
        }
    }
}
