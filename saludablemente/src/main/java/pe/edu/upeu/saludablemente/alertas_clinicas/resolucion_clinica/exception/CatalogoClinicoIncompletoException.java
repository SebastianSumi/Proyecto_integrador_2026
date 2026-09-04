package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception;

public class CatalogoClinicoIncompletoException extends ResolucionClinicaException {

    public CatalogoClinicoIncompletoException(String codigo) {
        super(
                String.format("Acción correctiva no encontrada en catálogo: %s", codigo),
                "CATALOGO_CLINICO_INCOMPLETO",
                "La acción correctiva seleccionada no está disponible en el sistema"
        );
    }

    public CatalogoClinicoIncompletoException(String codigo, Throwable cause) {
        super(
                String.format("Acción correctiva no encontrada en catálogo: %s", codigo),
                cause,
                "CATALOGO_CLINICO_INCOMPLETO",
                "La acción correctiva seleccionada no está disponible en el sistema"
        );
    }
}