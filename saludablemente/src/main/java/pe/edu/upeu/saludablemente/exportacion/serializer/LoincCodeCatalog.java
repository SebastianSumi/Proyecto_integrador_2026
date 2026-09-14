package pe.edu.upeu.saludablemente.exportacion.serializer;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoincCodeCatalog {

    public record CodigoEstandar(String loinc, String snomed, String display, String unidadUcum) {
    }

    private static final Map<String, CodigoEstandar> CATALOGO = Map.ofEntries(
            Map.entry("IMC", new CodigoEstandar("39156-5", "60621009", "Body mass index (BMI)", "kg/m2")),
            Map.entry("PESO", new CodigoEstandar("29463-7", "27113001", "Body weight", "kg")),
            Map.entry("TALLA", new CodigoEstandar("8302-2", "115363005", "Body height", "cm")),
            Map.entry("PERIMETRO_ABDOMINAL", new CodigoEstandar("56086-2", "248365001", "Abdominal circumference", "cm")),
            Map.entry("GRASA_CORPORAL", new CodigoEstandar("41982-0", "86249008", "Body fat percentage", "%")),
            Map.entry("GLUCOSA", new CodigoEstandar("1558-6", "33747003", "Glucose fasting serum", "mg/dL")),
            Map.entry("COLESTEROL_TOTAL", new CodigoEstandar("2093-3", "77068002", "Cholesterol total serum", "mg/dL")),
            Map.entry("COLESTEROL_HDL", new CodigoEstandar("2085-9", "87318009", "Cholesterol HDL serum", "mg/dL")),
            Map.entry("COLESTEROL_LDL", new CodigoEstandar("13457-7", "113947004", "Cholesterol LDL serum", "mg/dL")),
            Map.entry("TRIGLICERIDOS", new CodigoEstandar("2571-8", "85699006", "Triglycerides serum", "mg/dL")),
            Map.entry("PRESION_SISTOLICA", new CodigoEstandar("8480-6", "271649006", "Systolic blood pressure", "mm[Hg]")),
            Map.entry("PRESION_DIASTOLICA", new CodigoEstandar("8462-4", "271650006", "Diastolic blood pressure", "mm[Hg]"))
    );

    public CodigoEstandar obtener(String clave) {
        return CATALOGO.get(clave.toUpperCase());
    }

    public boolean existe(String clave) {
        return CATALOGO.containsKey(clave.toUpperCase());
    }
}
