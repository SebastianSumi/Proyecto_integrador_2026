package pe.edu.upeu.saludablemente.actividades.inscripcion.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoInscripcion {
    ENROLLED("INSCRITA"),
    CANCELLED("CANCELADA");

    private final String publicValue;

    EstadoInscripcion(String publicValue) {
        this.publicValue = publicValue;
    }

    @JsonValue
    public String publicValue() {
        return publicValue;
    }
}
