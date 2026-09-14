package pe.edu.upeu.saludablemente.exportacion.dto;

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
public class OpcionesExportacionDTO {

    private List<ColumnaSeleccionadaDTO> columnas;
    private boolean incluirDiccionario;
    private boolean incluirGraficos;
    private boolean incluirResumenEjecutivo;
    private boolean incluirPdfConsolidado;
    private boolean incluirMatrizDatos;
}
