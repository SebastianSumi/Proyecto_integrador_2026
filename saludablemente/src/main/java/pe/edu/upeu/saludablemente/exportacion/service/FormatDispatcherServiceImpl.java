package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.FormatoNoSoportadoException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;
import pe.edu.upeu.saludablemente.exportacion.serializer.SerializadorFormato;

import java.io.OutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@Primary
public class FormatDispatcherServiceImpl implements FormatDispatcherService {

    private final Map<FormatoSalida, SerializadorFormato> serializadores = new EnumMap<>(FormatoSalida.class);

    public FormatDispatcherServiceImpl(List<SerializadorFormato> listaSerializadores) {
        for (SerializadorFormato serializador : listaSerializadores) {
            serializadores.put(serializador.getFormatoSoportado(), serializador);
            log.info("Serializador registrado para formato: {}", serializador.getFormatoSoportado());
        }
    }

    @Override
    public ResultadoSerializacionDTO despachar(Stream<CohorteClinicaDTO> datos,
                                                FormatoSalida formato,
                                                OpcionesExportacionDTO opciones,
                                                OutputStream out) {
        SerializadorFormato serializador = serializadores.get(formato);
        if (serializador == null) {
            log.error("No existe serializador registrado para el formato {}", formato);
            throw new FormatoNoSoportadoException(formato.name());
        }

        log.debug("Despachando serializacion hacia {}", serializador.getClass().getSimpleName());
        return serializador.serializar(datos, opciones, out);
    }
}
