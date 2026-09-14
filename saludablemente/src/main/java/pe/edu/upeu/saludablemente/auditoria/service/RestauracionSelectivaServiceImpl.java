package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.entity.ManifiestoArchivadoFrioEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoManifiesto;
import pe.edu.upeu.saludablemente.auditoria.repository.ManifiestoArchivadoFrioRepository;
import pe.edu.upeu.saludablemente.auditoria.storage.ColdStorageAdapter;
import pe.edu.upeu.saludablemente.exception.ColdStorageOperationException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestauracionSelectivaServiceImpl implements RestauracionSelectivaService {

    private final ManifiestoArchivadoFrioRepository manifiestoRepository;
    private final ColdStorageAdapter coldStorageAdapter;
    private final HashLedgerService hashLedgerService;

    @Override
    public List<Map<String, Object>> restaurarRango(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Solicitando restauracion de rango {} a {}", inicio, fin);

        List<ManifiestoArchivadoFrioEntity> manifiestos = manifiestoRepository
                .findByEstadoOrderByFechaExportacionAsc(EstadoManifiesto.ARCHIVADO);

        List<Map<String, Object>> registrosRestaurados = new ArrayList<>();

        for (ManifiestoArchivadoFrioEntity manifiesto : manifiestos) {
            boolean intersecta = !manifiesto.getRangoFechaFin().isBefore(inicio)
                    && !manifiesto.getRangoFechaInicio().isAfter(fin);

            if (!intersecta) {
                continue;
            }

            try {
                List<Map<String, Object>> registros = restaurarDesdeManifiesto(manifiesto);
                registrosRestaurados.addAll(registros);

            } catch (Exception e) {
                log.error("Error al restaurar manifiesto {}: {}",
                        manifiesto.getIdManifiesto(), e.getMessage(), e);
            }
        }

        log.info("Restauracion completada. {} registros recuperados", registrosRestaurados.size());
        return registrosRestaurados;
    }

    @Override
    public byte[] descargarArchivoArchivado(String rutaColdStorage) {
        if (!coldStorageAdapter.existeArchivo(rutaColdStorage)) {
            throw new ColdStorageOperationException(
                    "Archivo no encontrado en cold storage: " + rutaColdStorage);
        }
        return coldStorageAdapter.obtenerArchivo(rutaColdStorage);
    }

    @Override
    public boolean verificarIntegridadArchivo(String rutaColdStorage, String hashEsperado) {
        try {
            byte[] contenido = descargarArchivoArchivado(rutaColdStorage);
            String hashCalculado = hashLedgerService.calcularHashSha256(
                    new String(contenido, StandardCharsets.UTF_8));

            boolean integro = hashCalculado.equals(hashEsperado);
            if (!integro) {
                log.error("Violacion de integridad en archivo {}: hash esperado {}, calculado {}",
                        rutaColdStorage, hashEsperado, hashCalculado);
            }
            return integro;

        } catch (Exception e) {
            log.error("Error al verificar integridad de archivo {}: {}",
                    rutaColdStorage, e.getMessage());
            return false;
        }
    }

    private List<Map<String, Object>> restaurarDesdeManifiesto(ManifiestoArchivadoFrioEntity manifiesto)
            throws Exception {
        byte[] contenido = coldStorageAdapter.obtenerArchivo(manifiesto.getRutaColdStorage());

        List<Map<String, Object>> registros = new ArrayList<>();

        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(contenido));
             Reader reader = new InputStreamReader(gzip, StandardCharsets.UTF_8)) {

            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int leidos;
            while ((leidos = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, leidos);
            }

            String[] lineas = sb.toString().split("\n");
            if (lineas.length < 2) {
                return registros;
            }

            String[] cabecera = lineas[0].split(",");

            for (int i = 1; i < lineas.length; i++) {
                String[] valores = lineas[i].split(",");
                if (valores.length != cabecera.length) {
                    continue;
                }

                Map<String, Object> registro = new HashMap<>();
                for (int j = 0; j < cabecera.length; j++) {
                    registro.put(cabecera[j], valores[j]);
                }
                registros.add(registro);
            }
        }

        log.info("Manifiesto {} restaurado: {} registros", manifiesto.getIdManifiesto(), registros.size());
        return registros;
    }
}
