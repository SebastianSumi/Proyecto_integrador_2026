package pe.edu.upeu.saludablemente.actividades.actividad.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum EstadoActividad {
    SCHEDULED("PROGRAMADA"),
    IN_PROGRESS("EN_CURSO"),
    FINISHED("FINALIZADA"),
    CANCELLED("CANCELADA");

    private final String publicValue;

    EstadoActividad(String publicValue) {
        this.publicValue = publicValue;
    }

    @JsonValue
    public String publicValue() {
        return publicValue;
    }

    @JsonCreator
    public static EstadoActividad fromPublicValue(String value) {
        return Arrays.stream(values())
                .filter(state -> state.publicValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown activity state: " + value));
    }
}
