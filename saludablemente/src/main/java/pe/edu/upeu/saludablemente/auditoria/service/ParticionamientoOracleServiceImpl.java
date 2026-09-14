package pe.edu.upeu.saludablemente.auditoria.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.ParticionInfoDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.ParticionBitacoraEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.ParticionBitacoraRepository;
import pe.edu.upeu.saludablemente.exception.ParticionamientoException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticionamientoOracleServiceImpl implements ParticionamientoOracleService {

    private static final DateTimeFormatter FORMATO_NOMBRE = DateTimeFormatter.ofPattern("yyyy_MM");

    private final ParticionBitacoraRepository particionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void crearParticionMensual(String tabla, LocalDateTime mes) {
        if (tabla == null || mes == null) {
            throw new ParticionamientoException("Tabla o mes nulos al crear particion");
        }

        String nombreParticion = construirNombreParticion(tabla, mes);
        LocalDateTime inicioMes = mes.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1);

        try {
            if (existeParticion(nombreParticion)) {
                log.info("La particion {} ya existe, se omite creacion", nombreParticion);
                return;
            }

            String ddl = String.format(
                    "ALTER TABLE %s ADD PARTITION %s VALUES LESS THAN (TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'))",
                    tabla,
                    nombreParticion,
                    finMes.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Query query = entityManager.createNativeQuery(ddl);
            query.executeUpdate();

            registrarParticionEnControl(tabla, nombreParticion, inicioMes, finMes);

            log.info("Particion {} creada exitosamente para tabla {}", nombreParticion, tabla);

        } catch (Exception e) {
            log.error("Error al crear particion {}: {}", nombreParticion, e.getMessage(), e);
            throw new ParticionamientoException("Error al crear particion " + nombreParticion, e);
        }
    }

    @Override
    @Transactional
    public void registrarParticionEnControl(String tabla, String nombreParticion,
                                            LocalDateTime inicio, LocalDateTime fin) {
        ParticionBitacoraEntity entidad = ParticionBitacoraEntity.builder()
                .nombreParticion(nombreParticion)
                .tablaOrigen(tabla)
                .rangoFechaInicio(inicio)
                .rangoFechaFin(fin)
                .estado("ACTIVA")
                .archivada("N")
                .purgada("N")
                .fechaCreacion(LocalDateTime.now())
                .build();

        particionRepository.save(entidad);
        log.debug("Particion registrada en control: {}", nombreParticion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticionInfoDTO> listarParticiones(String tabla) {
        List<ParticionInfoDTO> resultado = new ArrayList<>();

        try {
            String sql = "SELECT PARTITION_NAME, HIGH_VALUE FROM USER_TAB_PARTITIONS " +
                    "WHERE TABLE_NAME = :tabla ORDER BY PARTITION_POSITION";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("tabla", tabla.toUpperCase());

            @SuppressWarnings("unchecked")
            List<Object[]> filas = query.getResultList();

            for (Object[] fila : filas) {
                String nombre = (String) fila[0];

                resultado.add(ParticionInfoDTO.builder()
                        .nombreParticion(nombre)
                        .tablaOrigen(tabla)
                        .estado("ACTIVA")
                        .build());
            }
        } catch (Exception e) {
            log.error("Error al listar particiones de {}: {}", tabla, e.getMessage(), e);
        }

        return resultado;
    }

    @Override
    @Transactional
    public void sincronizarMetadatosParticiones(String tabla) {
        try {
            String sql = "SELECT PARTITION_NAME FROM USER_TAB_PARTITIONS WHERE TABLE_NAME = :tabla";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("tabla", tabla.toUpperCase());

            @SuppressWarnings("unchecked")
            List<String> particionesFisicas = query.getResultList();

            List<ParticionBitacoraEntity> particionesRegistradas =
                    particionRepository.findByTablaOrigenOrderByRangoFechaInicioAsc(tabla);

            List<String> nombresRegistrados = particionesRegistradas.stream()
                    .map(ParticionBitacoraEntity::getNombreParticion)
                    .toList();

            for (String nombreFisico : particionesFisicas) {
                if (!nombresRegistrados.contains(nombreFisico)) {
                    log.info("Particion fisica {} no registrada en control. Registrando...", nombreFisico);
                    registrarParticionEnControl(tabla, nombreFisico,
                            LocalDateTime.now().minusMonths(1), LocalDateTime.now());
                }
            }

            log.info("Sincronizacion de particiones completada para {}: {} fisicas, {} registradas",
                    tabla, particionesFisicas.size(), particionesRegistradas.size());

        } catch (Exception e) {
            log.error("Error al sincronizar particiones: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarParticion(String tabla, String nombreParticion) {
        try {
            String ddl = String.format("ALTER TABLE %s DROP PARTITION %s", tabla, nombreParticion);
            Query query = entityManager.createNativeQuery(ddl);
            query.executeUpdate();

            log.info("Particion {} eliminada de tabla {}", nombreParticion, tabla);

        } catch (Exception e) {
            log.error("Error al eliminar particion {}: {}", nombreParticion, e.getMessage(), e);
            throw new ParticionamientoException("Error al eliminar particion " + nombreParticion, e);
        }
    }

    private boolean existeParticion(String nombreParticion) {
        try {
            String sql = "SELECT COUNT(*) FROM USER_TAB_PARTITIONS WHERE PARTITION_NAME = :nombre";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("nombre", nombreParticion.toUpperCase());
            Long count = ((Number) query.getSingleResult()).longValue();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String construirNombreParticion(String tabla, LocalDateTime mes) {
        return tabla.toUpperCase() + "_" + mes.format(FORMATO_NOMBRE);
    }
}
