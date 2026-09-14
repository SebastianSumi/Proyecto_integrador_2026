package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.MetaBienestarItemDTO;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MetasClient {

    public List<MetaBienestarItemDTO> obtenerMetasActivas(Long idPersona) {
        log.debug("Obteniendo metas activas para persona {}", idPersona);
        List<MetaBienestarItemDTO> metas = new ArrayList<>();
        metas.add(MetaBienestarItemDTO.builder()
                .idMeta(1L)
                .descripcion("Reducir grasa visceral a nivel <= 9")
                .porcentajeAvance(60)
                .estado("EN_PROGRESO")
                .build());
        return metas;
    }
}
