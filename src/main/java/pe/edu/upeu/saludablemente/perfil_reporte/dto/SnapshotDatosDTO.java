package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotDatosDTO {
    private Map<String, Object> filiacion;
    private Map<String, Object> evaluacionActual;
    private Map<String, Object> analiticaHistorica;
    private String semaforoBienestarGlobal;
    private Map<String, Object> aptitudFisica;
    private List<Map<String, Object>> alertasActivas;
    private Map<String, Object> recomendacionIA;
    private List<Map<String, Object>> metas;
    private List<Map<String, Object>> agendaActividades;
    private String periodoSemestral;
    private String fechaGeneracion;
    private String idReporte;
    private String versionPlantilla;
}
