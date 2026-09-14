package pe.edu.upeu.saludablemente.auditoria.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.ManifiestoArchivadoDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.ManifiestoArchivadoFrioEntity;
import pe.edu.upeu.saludablemente.auditoria.entity.ParticionBitacoraEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.ParticionBitacoraRepository;
import pe.edu.upeu.saludablemente.auditoria.storage.ColdStorageAdapter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivadoFrioServiceImpl implements ArchivadoFrioService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_RUTA = DateTimeFormatter.ofPattern("yyyy/MM");

    private final ParticionBitacoraRepository particionRepository;
    private final CriptographicManifestBuilder manifestBuilder;
    private final ColdStorageAdapter coldStorageAdapter;

    @Value("${auditoria.retencion.dias-retencion-caliente:365}")
    private int diasRetencionCaliente;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<ManifiestoArchivadoDTO> archivarParticionesVencidas(String tabla) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasRetencionCaliente);

        log.info("Buscando particiones de {} candidatas a archivado (anteriores a {})", tabla, fechaLimite);

        List<ParticionBitacoraEntity> candidatas = particionRepository
                .findCandidatasArchivado(tabla, fechaLimite);

        if (candidatas.isEmpty()) {
            log.info("No hay particiones candidatas a archivado en {}", tabla);
            return List.of();
        }

        List<ManifiestoArchivadoDTO> manifiestos = new ArrayList<>();

        for (ParticionBitacoraEntity particion : candidatas) {
            try {
                ManifiestoArchivadoDTO manifiesto = archivarParticion(
                        tabla,
                        particion.getNombreParticion(),
                        particion.getRangoFechaInicio(),
                        particion.getRangoFechaFin());

                manifiestos.add(manifiesto);

            } catch (Exception e) {
                log.error("Error al archivar particion {}: {}", particion.getNombreParticion(), e.getMessage(), e);
            }
        }

        return manifiestos;
    }

    @Override
    @Transactional
    public ManifiestoArchivadoDTO archivarParticion(String tabla, String nombreParticion,
                                                    LocalDateTime inicio, LocalDateTime fin) {
        log.info("Archivando particion {} de {}", nombreParticion, tabla);

        try {
            byte[] contenidoComprimido = exportarYComprimirParticion(tabla, nombreParticion, inicio, fin);

            String rutaColdStorage = construirRutaColdStorage(tabla, inicio);

            coldStorageAdapter.guardarArchivo(rutaColdStorage, contenidoComprimido);

            ManifiestoArchivadoFrioEntity manifiesto = manifestBuilder.construirManifiesto(
                    inicio, fin,
                    contarRegistrosParticion(tabla, nombreParticion),
                    rutaColdStorage,
                    contenidoComprimido.length,
                    "sistema-scheduler");

            particionRepository.findByNombreParticion(nombreParticion).ifPresent(p -> {
                particionRepository.marcarComoArchivada(p.getIdParticion(), manifiesto.getIdManifiesto());
            });

            log.info("Particion {} archivada exitosamente en {}", nombreParticion, rutaColdStorage);

            return ManifiestoArchivadoDTO.builder()
                    .idManifiesto(manifiesto.getIdManifiesto())
                    .rangoFechaInicio(manifiesto.getRangoFechaInicio())
                    .rangoFechaFin(manifiesto.getRangoFechaFin())
                    .totalRegistrosExportados(manifiesto.getTotalRegistrosExportados())
                    .rutaColdStorage(manifiesto.getRutaColdStorage())
                    .hashManifiesto(manifiesto.getHashManifiesto())
                    .firmaRsa(manifiesto.getFirmaRsa())
                    .fechaExportacion(manifiesto.getFechaExportacion())
                    .responsablePurga(manifiesto.getResponsablePurga())
                    .estado(manifiesto.getEstado().name())
                    .build();

        } catch (Exception e) {
            log.error("Error al archivar particion {}: {}", nombreParticion, e.getMessage(), e);
            throw new RuntimeException("Error al archivar particion " + nombreParticion, e);
        }
    }

    private byte[] exportarYComprimirParticion(String tabla, String nombreParticion,
                                               LocalDateTime inicio, LocalDateTime fin) throws Exception {
        String sql = String.format(
                "SELECT * FROM %s PARTITION (%s) WHERE FECHA_REGISTRO >= TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') " +
                        "AND FECHA_REGISTRO < TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')",
                tabla, nombreParticion,
                inicio.format(FORMATO_FECHA),
                fin.format(FORMATO_FECHA));

        Query query = entityManager.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        List<Object[]> filas = query.getResultList();

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID_BITACORA,SECUENCIA,FECHA_REGISTRO,USUARIO_AUTOR,ENTIDAD_AFECTADA,TIPO_OPERACION\n");

        for (Object[] fila : filas) {
            csvBuilder.append(String.join(",",
                    valorOString(fila[0]), valorOString(fila[1]), valorOString(fila[2]),
                    valorOString(fila[3]), valorOString(fila[4]), valorOString(fila[5])
            )).append("\n");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(csvBuilder.toString().getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
    }

    private int contarRegistrosParticion(String tabla, String nombreParticion) {
        try {
            String sql = String.format("SELECT COUNT(*) FROM %s PARTITION (%s)", tabla, nombreParticion);
            Query query = entityManager.createNativeQuery(sql);
            return ((Number) query.getSingleResult()).intValue();
        } catch (Exception e) {
            log.warn("No se pudo contar registros de particion {}: {}", nombreParticion, e.getMessage());
            return 0;
        }
    }

    private String construirRutaColdStorage(String tabla, LocalDateTime inicio) {
        return String.format("%s/%s/%s_bitacora.csv.gz",
                tabla.toLowerCase(),
                inicio.format(FORMATO_RUTA),
                tabla.toLowerCase() + "_" + inicio.format(DateTimeFormatter.ofPattern("yyyy_MM")));
    }

    private String valorOString(Object valor) {
        return valor != null ? valor.toString() : "";
    }
}
