package pe.edu.upeu.saludablemente.auditoria.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.dto.CambioAtomicoDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.DiferencialJsonDTO;
import pe.edu.upeu.saludablemente.exception.BusinessException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonDiffCalculatorServiceImpl implements JsonDiffCalculatorService {

    private final ObjectMapper objectMapper;

    @Override
    public DiferencialJsonDTO calcularDiferencial(Map<String, Object> estadoAnterior,
                                                  Map<String, Object> estadoNuevo) {
        List<CambioAtomicoDTO> cambios = new ArrayList<>();

        if (estadoAnterior == null && estadoNuevo == null) {
            return DiferencialJsonDTO.builder()
                    .cambios(cambios)
                    .totalCambios(0)
                    .huboCambios(false)
                    .build();
        }

        if (estadoAnterior == null) {
            for (Map.Entry<String, Object> entry : estadoNuevo.entrySet()) {
                cambios.add(construirCambio(entry.getKey(), null, entry.getValue(), "INSERT"));
            }
        } else if (estadoNuevo == null) {
            for (Map.Entry<String, Object> entry : estadoAnterior.entrySet()) {
                cambios.add(construirCambio(entry.getKey(), entry.getValue(), null, "DELETE"));
            }
        } else {
            Set<String> claves = new HashSet<>();
            claves.addAll(estadoAnterior.keySet());
            claves.addAll(estadoNuevo.keySet());

            for (String clave : claves) {
                Object valorAnterior = estadoAnterior.get(clave);
                Object valorNuevo = estadoNuevo.get(clave);

                if (!Objects.equals(valorAnterior, valorNuevo)) {
                    String tipo = determinarTipoCambio(valorAnterior, valorNuevo);
                    cambios.add(construirCambio(clave, valorAnterior, valorNuevo, tipo));
                }
            }
        }

        return DiferencialJsonDTO.builder()
                .cambios(cambios)
                .totalCambios(cambios.size())
                .huboCambios(!cambios.isEmpty())
                .build();
    }

    @Override
    public String serializarDiferencial(DiferencialJsonDTO diferencial) {
        if (diferencial == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(diferencial);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar diferencial JSON", e);
            throw new BusinessException("Error al serializar el diferencial de cambios");
        }
    }

    @Override
    public String serializarMapa(Map<String, Object> mapa) {
        if (mapa == null || mapa.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(mapa);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar mapa a JSON", e);
            throw new BusinessException("Error al serializar snapshot");
        }
    }

    private CambioAtomicoDTO construirCambio(String campo, Object valorAnterior,
                                              Object valorNuevo, String tipoCambio) {
        return CambioAtomicoDTO.builder()
                .campo(campo)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .tipoCambio(tipoCambio)
                .build();
    }

    private String determinarTipoCambio(Object valorAnterior, Object valorNuevo) {
        if (valorAnterior == null && valorNuevo != null) {
            return "INSERT";
        }
        if (valorAnterior != null && valorNuevo == null) {
            return "DELETE";
        }
        return "UPDATE";
    }
}
