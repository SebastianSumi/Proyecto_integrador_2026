package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaActivaDTO {
    private String tipoIndicador;
    private String severidad;
    private String estado;
    private LocalDate fechaGeneracion;
    private String mensajeAmigable;
}
