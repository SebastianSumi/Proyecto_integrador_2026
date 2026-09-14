package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.AptitudFisicaDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AptitudFisicaMasivoClientSimulado implements AptitudFisicaMasivoClient {

    @Override
    public Map<Long, AptitudFisicaDTO> extraerPruebasFisicas(List<Long> idsPersona) {
        Map<Long, AptitudFisicaDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            mapa.put(id, AptitudFisicaDTO.builder()
                    .abdominales1Min(20 + (int) (id % 20))
                    .planchas1Min(15 + (int) (id % 15))
                    .saltoSinImpulsoCm(30.0 + (id % 15))
                    .carrera400mSegundos(80 + (int) (id % 30))
                    .nivelAptitud(id % 3 == 0 ? "AVANZADO" : "INTERMEDIO")
                    .build());
        }
        return mapa;
    }
}
