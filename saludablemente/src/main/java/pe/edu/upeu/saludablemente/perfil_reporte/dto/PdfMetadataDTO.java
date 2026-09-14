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
public class PdfMetadataDTO {
    private Long idPersona;
    private String periodoSemestral;
    private String idReporte;
    private String versionPlantilla;
    private String generadoPor;
}
