package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FhirResourceMapper {

    private final LoincCodeCatalog loincCatalog;

    public Map<String, Object> construirPatient(CohorteClinicaDTO cohorte) {
        FiliacionDTO f = cohorte.getFiliacion();
        if (f == null) {
            return new HashMap<>();
        }

        Map<String, Object> patient = new HashMap<>();
        patient.put("resourceType", "Patient");
        patient.put("id", f.getIdPersona() != null ? String.valueOf(f.getIdPersona()) : UUID.randomUUID().toString());

        List<Map<String, Object>> identifiers = new ArrayList<>();
        if (f.getCodigoColaborador() != null) {
            identifiers.add(Map.of(
                    "use", "official",
                    "system", "urn:saludablemente:colaborador",
                    "value", f.getCodigoColaborador()));
        }
        if (!identifiers.isEmpty()) {
            patient.put("identifier", identifiers);
        }

        Map<String, Object> name = new HashMap<>();
        name.put("use", "official");
        if (f.getNombreCompleto() != null) {
            name.put("text", f.getNombreCompleto());
        }
        patient.put("name", List.of(name));

        if (f.getSexo() != null) {
            patient.put("gender", "M".equalsIgnoreCase(f.getSexo()) ? "male" : "female");
        }

        patient.put("active", true);
        return patient;
    }

    public List<Map<String, Object>> construirObservations(CohorteClinicaDTO cohorte) {
        List<Map<String, Object>> observaciones = new ArrayList<>();

        String patientRef = cohorte.getFiliacion() != null && cohorte.getFiliacion().getIdPersona() != null
                ? "Patient/" + cohorte.getFiliacion().getIdPersona()
                : null;

        if (patientRef == null) {
            return observaciones;
        }

        if (cohorte.getAntropometria() != null) {
            agregarObservacion(observaciones, patientRef, "IMC", cohorte.getAntropometria().getImc());
            agregarObservacion(observaciones, patientRef, "PESO", cohorte.getAntropometria().getPesoKg());
            agregarObservacion(observaciones, patientRef, "TALLA", cohorte.getAntropometria().getTallaCm());
            agregarObservacion(observaciones, patientRef, "PERIMETRO_ABDOMINAL", cohorte.getAntropometria().getPerimetroAbdominalCm());
        }

        if (cohorte.getComposicionCorporal() != null) {
            agregarObservacion(observaciones, patientRef, "GRASA_CORPORAL", cohorte.getComposicionCorporal().getPorcentajeGrasa());
        }

        if (cohorte.getBioquimica() != null) {
            agregarObservacion(observaciones, patientRef, "GLUCOSA", cohorte.getBioquimica().getGlucosaMgDl());
            agregarObservacion(observaciones, patientRef, "COLESTEROL_TOTAL", cohorte.getBioquimica().getColesterolTotalMgDl());
            agregarObservacion(observaciones, patientRef, "COLESTEROL_HDL", cohorte.getBioquimica().getColesterolHdlMgDl());
            agregarObservacion(observaciones, patientRef, "COLESTEROL_LDL", cohorte.getBioquimica().getColesterolLdlMgDl());
            agregarObservacion(observaciones, patientRef, "TRIGLICERIDOS", cohorte.getBioquimica().getTrigliceridosMgDl());

            if (cohorte.getBioquimica().getPresionSistolica() != null) {
                agregarObservacion(observaciones, patientRef, "PRESION_SISTOLICA", Double.valueOf(cohorte.getBioquimica().getPresionSistolica()));
            }
            if (cohorte.getBioquimica().getPresionDiastolica() != null) {
                agregarObservacion(observaciones, patientRef, "PRESION_DIASTOLICA", Double.valueOf(cohorte.getBioquimica().getPresionDiastolica()));
            }
        }

        return observaciones;
    }

    private void agregarObservacion(List<Map<String, Object>> lista, String patientRef, String clave, Double valor) {
        if (valor == null) return;

        LoincCodeCatalog.CodigoEstandar codigo = loincCatalog.obtener(clave);
        if (codigo == null) {
            log.warn("No hay codigo LOINC para {}", clave);
            return;
        }

        Map<String, Object> obs = new HashMap<>();
        obs.put("resourceType", "Observation");
        obs.put("id", UUID.randomUUID().toString());
        obs.put("status", "final");

        Map<String, Object> codeMap = new HashMap<>();
        codeMap.put("coding", List.of(Map.of(
                "system", "http://loinc.org",
                "code", codigo.loinc(),
                "display", codigo.display()
        )));
        codeMap.put("text", codigo.display());
        obs.put("code", codeMap);

        obs.put("subject", Map.of("reference", patientRef));

        Map<String, Object> valueQuantity = new HashMap<>();
        valueQuantity.put("value", valor);
        valueQuantity.put("unit", codigo.unidadUcum());
        valueQuantity.put("system", "http://unitsofmeasure.org");
        valueQuantity.put("code", codigo.unidadUcum());
        obs.put("valueQuantity", valueQuantity);

        lista.add(obs);
    }
}
