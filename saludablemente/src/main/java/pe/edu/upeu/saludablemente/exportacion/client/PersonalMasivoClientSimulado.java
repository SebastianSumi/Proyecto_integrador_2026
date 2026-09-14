package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PersonalMasivoClientSimulado implements PersonalMasivoClient {

    private static final int POBLACION_SIMULADA = 5000;

    @Override
    public long contarPoblacion(FiltroPoblacionalParams filtros) {
        log.debug("[SIMULADO] Contando poblacion con filtros: {}", filtros);
        return POBLACION_SIMULADA;
    }

    @Override
    public List<Long> extraerIdsPaginados(FiltroPoblacionalParams filtros,
                                           Long ultimoId,
                                           int chunkSize) {
        long inicio = ultimoId == null ? 1 : ultimoId + 1;
        long fin = Math.min(inicio + chunkSize - 1, POBLACION_SIMULADA);

        if (inicio > POBLACION_SIMULADA) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (long i = inicio; i <= fin; i++) {
            ids.add(i);
        }
        log.debug("[SIMULADO] Extraidos {} IDs ({} a {})", ids.size(), inicio, fin);
        return ids;
    }

    @Override
    public List<FiliacionDTO> extraerFiliacion(List<Long> idsPersona) {
        List<FiliacionDTO> resultado = new ArrayList<>();
        for (Long id : idsPersona) {
            resultado.add(FiliacionDTO.builder()
                    .idPersona(id)
                    .codigoColaborador("EMP-" + (4000 + id))
                    .nombreCompleto("Colaborador Simulado " + id)
                    .areaTrabajo(id % 2 == 0 ? "Operaciones" : "Mantenimiento")
                    .sede(id % 3 == 0 ? "Planta_Norte" : "Central")
                    .edad(25 + (int) (id % 40))
                    .sexo(id % 2 == 0 ? "M" : "F")
                    .build());
        }
        log.debug("[SIMULADO] Extraida filiacion de {} colaboradores", resultado.size());
        return resultado;
    }
}
