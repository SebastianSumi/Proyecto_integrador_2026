package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.List;

public interface PersonalMasivoClient {

    long contarPoblacion(FiltroPoblacionalParams filtros);

    List<Long> extraerIdsPaginados(FiltroPoblacionalParams filtros,
                                    Long ultimoId,
                                    int chunkSize);

    List<FiliacionDTO> extraerFiliacion(List<Long> idsPersona);
}
