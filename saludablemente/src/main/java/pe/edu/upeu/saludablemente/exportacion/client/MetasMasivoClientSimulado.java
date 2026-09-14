package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.MetaBienestarDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MetasMasivoClientSimulado implements MetasMasivoClient {

    @Override
    public Map<Long, List<MetaBienestarDTO>> extraerMetasActivas(List<Long> idsPersona) {
        Map<Long, List<MetaBienestarDTO>> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            List<MetaBienestarDTO> metas = new ArrayList<>();
            metas.add(MetaBienestarDTO.builder()
                    .idMeta(id * 10 + 1)
                    .descripcion("Reducir grasa visceral a nivel <= 9")
                    .porcentajeAvance(60)
                    .estado("EN_PROGRESO")
                    .build());
            mapa.put(id, metas);
        }
        return mapa;
    }
}
