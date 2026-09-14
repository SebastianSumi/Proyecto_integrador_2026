package pe.edu.upeu.saludablemente.exportacion.client;

import pe.edu.upeu.saludablemente.exportacion.dto.AlertaActivaDTO;

import java.util.List;
import java.util.Map;

public interface AlertasMasivoClient {

    Map<Long, List<AlertaActivaDTO>> extraerAlertasActivas(List<Long> idsPersona);
}
