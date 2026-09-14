package pe.edu.upeu.saludablemente.exportacion.serializer;

import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

public final class ResultadoSerializacionHelper {

    private ResultadoSerializacionHelper() {
    }

    public static ResultadoSerializacionDTO construir(FormatoSalida formato,
                                                       int totalRegistros,
                                                       long tiempoMs,
                                                       Integer totalPaginas) {
        return ResultadoSerializacionDTO.builder()
                .formato(formato)
                .totalRegistros(totalRegistros)
                .tiempoSerializacionMs(tiempoMs)
                .totalPaginas(totalPaginas)
                .build();
    }
}
