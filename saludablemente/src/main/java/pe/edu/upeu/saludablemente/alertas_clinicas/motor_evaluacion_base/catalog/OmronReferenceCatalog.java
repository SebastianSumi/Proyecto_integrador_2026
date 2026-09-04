package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.catalog;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class OmronReferenceCatalog {

    // Rangos de IMC
    private static final BigDecimal IMC_NORMAL_MIN = new BigDecimal("18.5");
    private static final BigDecimal IMC_NORMAL_MAX = new BigDecimal("24.9");
    private static final BigDecimal IMC_SOBREPESO_MIN = new BigDecimal("25.0");
    private static final BigDecimal IMC_SOBREPESO_MAX = new BigDecimal("29.9");
    private static final BigDecimal IMC_OBESIDAD_MIN = new BigDecimal("30.0");

    // Rangos de Grasa Visceral (OMRON)
    private static final Map<String, Map<String, BigDecimal>> GRASA_VISCERAL_RANGOS = new HashMap<>();

    static {
        // Hombres
        Map<String, BigDecimal> hombreRangos = new HashMap<>();
        hombreRangos.put("normal_max", new BigDecimal("9"));
        hombreRangos.put("exceso_min", new BigDecimal("10"));
        hombreRangos.put("exceso_max", new BigDecimal("14"));
        hombreRangos.put("obesidad_min", new BigDecimal("15"));
        GRASA_VISCERAL_RANGOS.put("M", hombreRangos);

        // Mujeres
        Map<String, BigDecimal> mujerRangos = new HashMap<>();
        mujerRangos.put("normal_max", new BigDecimal("6"));
        mujerRangos.put("exceso_min", new BigDecimal("7"));
        mujerRangos.put("exceso_max", new BigDecimal("11"));
        mujerRangos.put("obesidad_min", new BigDecimal("12"));
        GRASA_VISCERAL_RANGOS.put("F", mujerRangos);
    }

    @Cacheable("omron_catalog")
    public BigDecimal getImcNormalMax() {
        return IMC_NORMAL_MAX;
    }

    @Cacheable("omron_catalog")
    public BigDecimal getImcSobrepesoMin() {
        return IMC_SOBREPESO_MIN;
    }

    @Cacheable("omron_catalog")
    public BigDecimal getImcObesidadMin() {
        return IMC_OBESIDAD_MIN;
    }

    @Cacheable(value = "omron_catalog", key = "'grasa_visceral_' + #sexo")
    public Map<String, BigDecimal> getRangosGrasaVisceral(String sexo) {
        return GRASA_VISCERAL_RANGOS.getOrDefault(sexo, GRASA_VISCERAL_RANGOS.get("M"));
    }

    @Cacheable(value = "omron_catalog", key = "'grasa_visceral_normal_max_' + #sexo")
    public BigDecimal getGrasaVisceralNormalMax(String sexo) {
        Map<String, BigDecimal> rangos = getRangosGrasaVisceral(sexo);
        return rangos.getOrDefault("normal_max", new BigDecimal("9"));
    }

    // Rangos estáticos para bioquímica
    @Cacheable("omron_catalog")
    public BigDecimal getGlucosaNormalMax() {
        return new BigDecimal("100.0"); // mg/dL
    }

    @Cacheable("omron_catalog")
    public BigDecimal getGlucosaPreDiabetesMin() {
        return new BigDecimal("100.0");
    }

    @Cacheable("omron_catalog")
    public BigDecimal getGlucosaDiabetesMin() {
        return new BigDecimal("126.0");
    }

    @Cacheable("omron_catalog")
    public BigDecimal getColesterolTotalNormalMax() {
        return new BigDecimal("200.0"); // mg/dL
    }

    @Cacheable("omron_catalog")
    public BigDecimal getColesterolTotalAltoMin() {
        return new BigDecimal("200.0");
    }

    @Cacheable("omron_catalog")
    public BigDecimal getTrigliceridosNormalMax() {
        return new BigDecimal("150.0"); // mg/dL
    }

    @Cacheable("omron_catalog")
    public BigDecimal getTrigliceridosAltoMin() {
        return new BigDecimal("150.0");
    }

    @Cacheable("omron_catalog")
    public BigDecimal getPresionSistolicaNormalMax() {
        return new BigDecimal("120.0"); // mmHg
    }

    @Cacheable("omron_catalog")
    public BigDecimal getPresionDiastolicaNormalMax() {
        return new BigDecimal("80.0"); // mmHg
    }
}