package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.ActividadAgendaDTO;

import java.util.List;
import java.util.Map;

public interface ActividadesMasivoClient {

    Map<Long, List<ActividadAgendaDTO>> extraerActividadesAsistidas(List<Long> idsPersona);
}
