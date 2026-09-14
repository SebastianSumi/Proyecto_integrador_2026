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
public class EstadoExportacionResponseDTO {

    private UUID idTarea;
    private String tituloLote;
    private String estado;
    private String formato;
    private String modoPrivacidad;
    private Integer porcentajeAvance;
    private String faseActual;
    private Long totalRegistrosExportados;
    private Long tamanoBytes;
    private String tamanoFormateado;
    private String hashSha256;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
    private Long tiempoTotalProcesamientoSegundos;
    private boolean estaCifrado;
    private String urlDescargaDirecta;
    private String urlPrefirmadaS3;
    private LocalDateTime expiracionDescarga;
    private String mensajeError;
}
