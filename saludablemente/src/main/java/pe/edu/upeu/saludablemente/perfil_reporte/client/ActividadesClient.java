package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ProximaActividadDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ActividadesClient {

    public List<ProximaActividadDTO> obtenerProximasActividades(Long idPersona) {
        log.debug("Obteniendo proximas actividades para persona {}", idPersona);
        List<ProximaActividadDTO> actividades = new ArrayList<>();
        actividades.add(ProximaActividadDTO.builder()
                .idActividad(101L)
                .titulo("Taller de Nutricion y Lectura de Etiquetas")
                .fecha(LocalDateTime.now().plusDays(3))
                .modalidad("Virtual")
                .ubicacion("Zoom")
                .build());
        return actividades;
    }
}
