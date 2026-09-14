package pe.edu.upeu.saludablemente.auditoria.config;

import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Getter
@Configuration
public class GeoIpConfig {

    @Value("${auditoria.geoip.database-path:./geoip/GeoLite2-City.mmdb}")
    private String databasePathConfig;

    @Value("${auditoria.geoip.enabled:true}")
    private boolean geoIpEnabled;

    private Path databasePathAbsoluta;

    @PostConstruct
    public void inicializar() {
        this.databasePathAbsoluta = resolverRutaAbsoluta(databasePathConfig);
        log.info("Ruta de base de datos GeoIP: {}", databasePathAbsoluta);
        if (geoIpEnabled && !databasePathAbsoluta.toFile().exists()) {
            log.warn("Base de datos GeoIP no encontrada en {}. "
                    + "El detector ImpossibleTravel quedara inactivo.", databasePathAbsoluta);
        }
    }

    @Bean(name = "geoIpDatabaseReader")
    public DatabaseReader geoIpDatabaseReader() {
        if (!geoIpEnabled) {
            log.warn("GeoLite2 deshabilitado por configuracion");
            return null;
        }

        File database = databasePathAbsoluta.toFile();
        if (!database.exists()) {
            log.warn("Base de datos GeoLite2 no encontrada en: {}", databasePathAbsoluta);
            return null;
        }

        try {
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            log.info("GeoLite2 cargado exitosamente desde: {}", databasePathAbsoluta);
            return reader;
        } catch (Exception e) {
            log.error("Error al cargar base de datos GeoLite2: {}", e.getMessage(), e);
            return null;
        }
    }

    private Path resolverRutaAbsoluta(String rutaConfigurada) {
        Path path = Paths.get(rutaConfigurada);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
