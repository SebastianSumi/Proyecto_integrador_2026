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
public class CohorteClinicaDTO {

    private FiliacionDTO filiacion;
    private AntropometriaDTO antropometria;
    private ComposicionCorporalDTO composicionCorporal;
    private BioquimicaHemodinamicaDTO bioquimica;
    private AptitudFisicaDTO aptitudFisica;
    private AnaliticaHistoricaDTO analitica;
    private List<AlertaActivaDTO> alertas;
    private RecomendacionVigenteDTO recomendacionIA;
    private List<MetaBienestarDTO> metas;
    private List<ActividadAgendaDTO> talleresAsistidos;
}
