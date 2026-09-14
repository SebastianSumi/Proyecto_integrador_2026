package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.AlertaActivaDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AlertasMasivoClientSimulado implements AlertasMasivoClient {

    @Override
    public Map<Long, List<AlertaActivaDTO>> extraerAlertasActivas(List<Long> idsPersona) {
        Map<Long, List<AlertaActivaDTO>> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            List<AlertaActivaDTO> alertas = new ArrayList<>();
            if (id % 3 == 0) {
                alertas.add(AlertaActivaDTO.builder()
                        .tipoIndicador("Grasa Visceral")
                        .severidad("WARNING")
                        .estado("EN_REVISION")
                        .fechaGeneracion(LocalDate.now().minusDays(id % 30))
                        .mensajeAmigable("Tu nivel de grasa visceral se encuentra ligeramente elevado. "
                                + "El area de nutricion ha preparado pautas para ti.")
                        .build());
            }
            if (id % 5 == 0) {
                alertas.add(AlertaActivaDTO.builder()
                        .tipoIndicador("Glucosa")
                        .severidad("WARNING")
                        .estado("EN_REVISION")
                        .fechaGeneracion(LocalDate.now().minusDays(id % 30))
                        .mensajeAmigable("Tus niveles de glucosa merecen seguimiento. "
                                + "Revisa la recomendacion de dieta.")
                        .build());
            }
            mapa.put(id, alertas);
        }
        return mapa;
    }
}
