package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDescargaStreamDTO {
    private UUID idReporte;
    private String periodoSemestral;
    private Long tamanoBytes;
    private String rutaAlmacenamiento;
    private String tipoAlmacenamiento;
}
