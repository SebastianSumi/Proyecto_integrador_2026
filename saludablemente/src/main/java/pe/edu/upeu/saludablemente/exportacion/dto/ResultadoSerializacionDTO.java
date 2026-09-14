package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoSerializacionDTO {

    private String rutaArchivoTemporal;
    private String nombreArchivoFinal;
    private FormatoSalida formato;
    private long tamanioBytes;
    private String hashSha256;
    private int totalRegistros;
    private Integer totalPaginas;
    private long tiempoSerializacionMs;
}
