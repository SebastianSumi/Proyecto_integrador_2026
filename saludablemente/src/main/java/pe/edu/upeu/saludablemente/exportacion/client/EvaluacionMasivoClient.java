package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.AnaliticaHistoricaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.AntropometriaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.BioquimicaHemodinamicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ComposicionCorporalDTO;

import java.util.List;
import java.util.Map;

public interface EvaluacionMasivoClient {

    Map<Long, AntropometriaDTO> extraerAntropometria(List<Long> idsPersona);

    Map<Long, ComposicionCorporalDTO> extraerComposicionCorporal(List<Long> idsPersona);

    Map<Long, BioquimicaHemodinamicaDTO> extraerBioquimica(List<Long> idsPersona);

    Map<Long, AnaliticaHistoricaDTO> extraerAnaliticaHistorica(List<Long> idsPersona);
}
