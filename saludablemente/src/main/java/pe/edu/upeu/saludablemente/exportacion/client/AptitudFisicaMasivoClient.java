package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.AptitudFisicaDTO;

import java.util.List;
import java.util.Map;

public interface AptitudFisicaMasivoClient {

    Map<Long, AptitudFisicaDTO> extraerPruebasFisicas(List<Long> idsPersona);
}
