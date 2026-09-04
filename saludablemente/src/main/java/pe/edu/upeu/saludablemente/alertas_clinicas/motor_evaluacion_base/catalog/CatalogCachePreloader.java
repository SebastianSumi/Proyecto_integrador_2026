package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogCachePreloader {

    private final OmronReferenceCatalog omronCatalog;

    @EventListener(ApplicationReadyEvent.class)
    public void preloadCatalogs() {
        log.info("🔄 Precargando catálogos clínicos en caché...");

        // Precargar todos los rangos comunes
        omronCatalog.getImcNormalMax();
        omronCatalog.getImcSobrepesoMin();
        omronCatalog.getImcObesidadMin();
        omronCatalog.getRangosGrasaVisceral("M");
        omronCatalog.getRangosGrasaVisceral("F");
        omronCatalog.getGlucosaNormalMax();
        omronCatalog.getColesterolTotalNormalMax();
        omronCatalog.getTrigliceridosNormalMax();
        omronCatalog.getPresionSistolicaNormalMax();
        omronCatalog.getPresionDiastolicaNormalMax();

        log.info("✅ Catálogos clínicos precargados exitosamente en caché");
    }
}