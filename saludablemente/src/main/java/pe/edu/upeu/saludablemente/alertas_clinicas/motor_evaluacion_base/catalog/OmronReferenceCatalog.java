package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.catalog;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class OmronReferenceCatalog {

    // =========================
    // Rangos de IMC
    // =========================

    private static final BigDecimal IMC_NORMAL_MIN = new BigDecimal("18.5");
    private static final BigDecimal IMC_NORMAL_MAX = new BigDecimal("24.9");

    private static final BigDecimal IMC_SOBREPESO_MIN = new BigDecimal("25.0");
    private static final BigDecimal IMC_SOBREPESO_MAX = new BigDecimal("29.9");

    private static final BigDecimal IMC_OBESIDAD_MIN = new BigDecimal("30.0");

    // =========================
    // Rangos de Grasa Visceral OMRON
    // =========================

    private static final Map<String, Map<String, BigDecimal>> GRASA_VISCERAL_RANGOS =
            new HashMap<>();

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

    // =========================
    // IMC - Getters
    // =========================

    @Cacheable(value = "omron_catalog", key = "'imc_normal_min'")
    public BigDecimal getImcNormalMin() {
        return IMC_NORMAL_MIN;
    }

    @Cacheable(value = "omron_catalog", key = "'imc_normal_max'")
    public BigDecimal getImcNormalMax() {
        return IMC_NORMAL_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'imc_sobrepeso_min'")
    public BigDecimal getImcSobrepesoMin() {
        return IMC_SOBREPESO_MIN;
    }

    @Cacheable(value = "omron_catalog", key = "'imc_sobrepeso_max'")
    public BigDecimal getImcSobrepesoMax() {
        return IMC_SOBREPESO_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'imc_obesidad_min'")
    public BigDecimal getImcObesidadMin() {
        return IMC_OBESIDAD_MIN;
    }

    // =========================
    // Grasa Visceral - Getters
    // =========================

    @Cacheable(
            value = "omron_catalog",
            key = "'grasa_visceral_' + #sexo"
    )
    public Map<String, BigDecimal> getRangosGrasaVisceral(String sexo) {
        return GRASA_VISCERAL_RANGOS.getOrDefault(
                sexo,
                GRASA_VISCERAL_RANGOS.get("M")
        );
    }

    @Cacheable(
            value = "omron_catalog",
            key = "'grasa_visceral_normal_max_' + #sexo"
    )
    public BigDecimal getGrasaVisceralNormalMax(String sexo) {
        return getRangosGrasaVisceral(sexo)
                .get("normal_max");
    }

    @Cacheable(
            value = "omron_catalog",
            key = "'grasa_visceral_exceso_min_' + #sexo"
    )
    public BigDecimal getGrasaVisceralExcesoMin(String sexo) {
        return getRangosGrasaVisceral(sexo)
                .get("exceso_min");
    }

    @Cacheable(
            value = "omron_catalog",
            key = "'grasa_visceral_exceso_max_' + #sexo"
    )
    public BigDecimal getGrasaVisceralExcesoMax(String sexo) {
        return getRangosGrasaVisceral(sexo)
                .get("exceso_max");
    }

    @Cacheable(
            value = "omron_catalog",
            key = "'grasa_visceral_obesidad_min_' + #sexo"
    )
    public BigDecimal getGrasaVisceralObesidadMin(String sexo) {
        return getRangosGrasaVisceral(sexo)
                .get("obesidad_min");
    }

    // =========================
    // Bioquímica - Glucosa
    // =========================

    private static final BigDecimal GLUCOSA_NORMAL_MAX =
            new BigDecimal("100.0");

    private static final BigDecimal GLUCOSA_PREDIABETES_MIN =
            new BigDecimal("100.0");

    private static final BigDecimal GLUCOSA_DIABETES_MIN =
            new BigDecimal("126.0");

    @Cacheable(value = "omron_catalog", key = "'glucosa_normal_max'")
    public BigDecimal getGlucosaNormalMax() {
        return GLUCOSA_NORMAL_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'glucosa_prediabetes_min'")
    public BigDecimal getGlucosaPreDiabetesMin() {
        return GLUCOSA_PREDIABETES_MIN;
    }

    @Cacheable(value = "omron_catalog", key = "'glucosa_diabetes_min'")
    public BigDecimal getGlucosaDiabetesMin() {
        return GLUCOSA_DIABETES_MIN;
    }

    // =========================
    // Bioquímica - Colesterol
    // =========================

    private static final BigDecimal COLESTEROL_TOTAL_NORMAL_MAX =
            new BigDecimal("200.0");

    private static final BigDecimal COLESTEROL_TOTAL_ALTO_MIN =
            new BigDecimal("200.0");

    @Cacheable(value = "omron_catalog", key = "'colesterol_total_normal_max'")
    public BigDecimal getColesterolTotalNormalMax() {
        return COLESTEROL_TOTAL_NORMAL_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'colesterol_total_alto_min'")
    public BigDecimal getColesterolTotalAltoMin() {
        return COLESTEROL_TOTAL_ALTO_MIN;
    }

    // =========================
    // Bioquímica - Triglicéridos
    // =========================

    private static final BigDecimal TRIGLICERIDOS_NORMAL_MAX =
            new BigDecimal("150.0");

    private static final BigDecimal TRIGLICERIDOS_ALTO_MIN =
            new BigDecimal("150.0");

    @Cacheable(value = "omron_catalog", key = "'trigliceridos_normal_max'")
    public BigDecimal getTrigliceridosNormalMax() {
        return TRIGLICERIDOS_NORMAL_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'trigliceridos_alto_min'")
    public BigDecimal getTrigliceridosAltoMin() {
        return TRIGLICERIDOS_ALTO_MIN;
    }

    // =========================
    // Presión arterial
    // =========================

    private static final BigDecimal PRESION_SISTOLICA_NORMAL_MAX =
            new BigDecimal("120.0");

    private static final BigDecimal PRESION_DIASTOLICA_NORMAL_MAX =
            new BigDecimal("80.0");

    @Cacheable(value = "omron_catalog", key = "'presion_sistolica_normal_max'")
    public BigDecimal getPresionSistolicaNormalMax() {
        return PRESION_SISTOLICA_NORMAL_MAX;
    }

    @Cacheable(value = "omron_catalog", key = "'presion_diastolica_normal_max'")
    public BigDecimal getPresionDiastolicaNormalMax() {
        return PRESION_DIASTOLICA_NORMAL_MAX;
    }
}