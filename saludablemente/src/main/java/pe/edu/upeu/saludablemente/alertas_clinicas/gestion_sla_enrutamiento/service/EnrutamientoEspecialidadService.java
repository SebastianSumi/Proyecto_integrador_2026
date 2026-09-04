package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
public class EnrutamientoEspecialidadService {

    // Indicadores que van a Nutrición
    private static final Set<TipoIndicador> INDICADORES_NUTRICION = EnumSet.of(
            TipoIndicador.IMC,
            TipoIndicador.GRASA_VISCERAL
    );

    // Indicadores que van a Medicina Ocupacional
    private static final Set<TipoIndicador> INDICADORES_MEDICINA = EnumSet.of(
            TipoIndicador.GLUCOSA,
            TipoIndicador.COLESTEROL_TOTAL,
            TipoIndicador.COLESTEROL_HDL,
            TipoIndicador.COLESTEROL_LDL,
            TipoIndicador.TRIGLICERIDOS,
            TipoIndicador.PRESION_SISTOLICA,
            TipoIndicador.PRESION_DIASTOLICA,
            TipoIndicador.RIESGO_METABOLICO,
            TipoIndicador.RIESGO_CARDIOVASCULAR
    );

    public enum Especialidad {
        NUTRICION("Nutrición"),
        MEDICINA_OCUPACIONAL("Medicina Ocupacional"),
        GENERAL("General");

        private final String displayName;

        Especialidad(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Determina la especialidad basada en el tipo de indicador
     */
    public Especialidad determinarEspecialidad(TipoIndicador tipoIndicador) {
        if (tipoIndicador == null) {
            return Especialidad.GENERAL;
        }

        if (INDICADORES_NUTRICION.contains(tipoIndicador)) {
            log.debug(" Indicador {} enrutado a Nutrición", tipoIndicador);
            return Especialidad.NUTRICION;
        }

        if (INDICADORES_MEDICINA.contains(tipoIndicador)) {
            log.debug(" Indicador {} enrutado a Medicina Ocupacional", tipoIndicador);
            return Especialidad.MEDICINA_OCUPACIONAL;
        }

        return Especialidad.GENERAL;
    }

    /**
     * Determina la especialidad basada en el primer indicador de la lista
     */
    public Especialidad determinarEspecialidadPorIndicadores(Set<TipoIndicador> indicadores) {
        if (indicadores == null || indicadores.isEmpty()) {
            return Especialidad.GENERAL;
        }

        // Si hay indicadores de ambos tipos, priorizar el más grave
        boolean tieneNutricion = indicadores.stream().anyMatch(INDICADORES_NUTRICION::contains);
        boolean tieneMedicina = indicadores.stream().anyMatch(INDICADORES_MEDICINA::contains);

        if (tieneNutricion && tieneMedicina) {
            log.warn(" Alerta con indicadores mixtos - Asignando a Medicina Ocupacional por prioridad");
            return Especialidad.MEDICINA_OCUPACIONAL;
        }

        if (tieneMedicina) {
            return Especialidad.MEDICINA_OCUPACIONAL;
        }

        if (tieneNutricion) {
            return Especialidad.NUTRICION;
        }

        return Especialidad.GENERAL;
    }

    /**
     * Obtiene los indicadores de una especialidad
     */
    public Set<TipoIndicador> getIndicadoresPorEspecialidad(Especialidad especialidad) {
        return switch (especialidad) {
            case NUTRICION -> INDICADORES_NUTRICION;
            case MEDICINA_OCUPACIONAL -> INDICADORES_MEDICINA;
            case GENERAL -> EnumSet.allOf(TipoIndicador.class);
        };
    }
}