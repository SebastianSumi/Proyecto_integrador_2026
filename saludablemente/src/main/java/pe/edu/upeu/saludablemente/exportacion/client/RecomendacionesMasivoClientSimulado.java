package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.RecomendacionVigenteDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class RecomendacionesMasivoClientSimulado implements RecomendacionesMasivoClient {

    @Override
    public Map<Long, RecomendacionVigenteDTO> extraerRecomendacionVigente(List<Long> idsPersona) {
        Map<Long, RecomendacionVigenteDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            mapa.put(id, RecomendacionVigenteDTO.builder()
                    .idRecomendacion(UUID.randomUUID().toString())
                    .vigente(true)
                    .enfoquePrincipal("Resistencia Mixta y Movilidad")
                    .frecuenciaSemanalDias(3)
                    .estrategiaNutricional("Control de indice glucemico y balance hidrico")
                    .build());
        }
        return mapa;
    }
}
