package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.MetaBienestarDTO;

import java.util.List;
import java.util.Map;

public interface MetasMasivoClient {

    Map<Long, List<MetaBienestarDTO>> extraerMetasActivas(List<Long> idsPersona);
}
