package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioAtomicoViewDTO {
    private String campo;
    private Object valorAnterior;
    private Object valorNuevo;
    private String tipoCambio;
}
