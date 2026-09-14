package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CatalogCachePreloader {

    private static final Logger log = LoggerFactory.getLogger(CatalogCachePreloader.class);
    private final OmronReferenceCatalog catalog;

    public CatalogCachePreloader(OmronReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void preloadCache() {
        log.info("[Alertas Clinicas] Precalentando cache Caffeine de catalogos OMRON...");

        // Precalentar IMC
        catalog.getImcNormalMin();
        catalog.getImcNormalMax();
        catalog.getImcSobrepesoMin();
        catalog.getImcSobrepesoMax();
        catalog.getImcObesidadMin();

        // Precalentar Grasa Visceral
        catalog.getGrasaVisceralNormalMax("M");
        catalog.getGrasaVisceralExcesoMin("M");
        catalog.getGrasaVisceralExcesoMax("M");
        catalog.getGrasaVisceralObesidadMin("M");

        catalog.getGrasaVisceralNormalMax("F");
        catalog.getGrasaVisceralExcesoMin("F");
        catalog.getGrasaVisceralExcesoMax("F");
        catalog.getGrasaVisceralObesidadMin("F");

        // Precalentar Bioquimica
        catalog.getGlucosaNormalMax();
        catalog.getGlucosaPreDiabetesMin();
        catalog.getGlucosaDiabetesMin();

        catalog.getColesterolTotalNormalMax();
        catalog.getColesterolTotalAltoMin();

        catalog.getTrigliceridosNormalMax();
        catalog.getTrigliceridosAltoMin();

        // Precalentar Presion Arterial
        catalog.getPresionSistolicaNormalMax();
        catalog.getPresionDiastolicaNormalMax();

        log.info("[Alertas Clinicas] Precalentamiento de catalogo OMRON completado con exito (< 1ms).");
    }
}
