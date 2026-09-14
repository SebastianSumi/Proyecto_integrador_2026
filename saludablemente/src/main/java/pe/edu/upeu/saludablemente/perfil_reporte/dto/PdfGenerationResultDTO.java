package pe.edu.upeu.saludablemente.perfil_reporte.dto;

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
public class PdfGenerationResultDTO {
    private byte[] pdfBytes;
    private Long tamanoBytes;
    private String hashSha256;
    private Integer totalPaginas;
    private Long tiempoCompilacionMs;
}
