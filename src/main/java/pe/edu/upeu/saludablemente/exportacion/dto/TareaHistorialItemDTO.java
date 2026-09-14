package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaHistorialItemDTO {

    private UUID idTarea;
    private String tituloLote;
    private String estado;
    private String formato;
    private String modoPrivacidad;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaFinalizacion;
    private Long totalRegistros;
    private String tamanoFormateado;
    private boolean disponibleParaDescarga;
}
