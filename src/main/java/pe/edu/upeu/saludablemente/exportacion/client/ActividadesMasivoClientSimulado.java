package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.ActividadAgendaDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ActividadesMasivoClientSimulado implements ActividadesMasivoClient {

    @Override
    public Map<Long, List<ActividadAgendaDTO>> extraerActividadesAsistidas(List<Long> idsPersona) {
        Map<Long, List<ActividadAgendaDTO>> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            List<ActividadAgendaDTO> actividades = new ArrayList<>();
            actividades.add(ActividadAgendaDTO.builder()
                    .idActividad(100 + (id % 10))
                    .titulo("Taller de Nutricion y Lectura de Etiquetas")
                    .fecha(LocalDateTime.now().minusDays(id % 60))
                    .modalidad("Virtual")
                    .build());
            mapa.put(id, actividades);
        }
        return mapa;
    }
}
