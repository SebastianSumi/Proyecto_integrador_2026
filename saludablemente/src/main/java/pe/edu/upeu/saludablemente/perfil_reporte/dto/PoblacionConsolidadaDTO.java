package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoblacionConsolidadaDTO {
    private ResumenEjecutivoDTO resumen;
    private List<PerfilDashboardResponseDTO> colaboradores;
    private String usuarioSolicitante;
    private String idTareaExportacion;
}
