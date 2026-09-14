package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.RecomendacionVigenteDTO;

import java.util.List;
import java.util.Map;

public interface RecomendacionesMasivoClient {

    Map<Long, RecomendacionVigenteDTO> extraerRecomendacionVigente(List<Long> idsPersona);
}
