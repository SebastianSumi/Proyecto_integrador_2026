package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.client.PersonalMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class CursorPaginationProvider {

    private final PersonalMasivoClient personalClient;

    public Stream<List<Long>> iterarIdsPaginados(FiltroPoblacionalParams filtros, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("El tamano de bloque debe ser positivo");
        }

        Iterator<List<Long>> iterador = new Iterator<>() {
            private Long ultimoId = null;
            private List<Long> siguienteBloque = null;
            private boolean terminado = false;

            @Override
            public boolean hasNext() {
                if (terminado) {
                    return false;
                }
                if (siguienteBloque == null) {
                    siguienteBloque = personalClient.extraerIdsPaginados(filtros, ultimoId, chunkSize);
                    if (siguienteBloque == null || siguienteBloque.isEmpty()) {
                        terminado = true;
                        return false;
                    }
                }
                return true;
            }

            @Override
            public List<Long> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No hay mas bloques");
                }
                List<Long> bloque = siguienteBloque;
                ultimoId = bloque.get(bloque.size() - 1);
                siguienteBloque = null;
                return bloque;
            }
        };

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterador, Spliterator.ORDERED),
                false);
    }
}
