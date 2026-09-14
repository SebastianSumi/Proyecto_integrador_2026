package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteEvidenciaDTO {
    private String nombreArchivo;
    private String formato;
    private byte[] contenido;
    private long tamanoBytes;
    private String hashSha256;
    private String firmaRsa;
    private LocalDateTime fechaGeneracion;
    private String solicitante;
    private String justificacionLegal;
    private long totalRegistrosIncluidos;
}
