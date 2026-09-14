package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DataMaskingServiceImpl implements DataMaskingService {

    private static final String MASCARA = "*****";

    private static final Set<String> CAMPOS_SENSIBLES = Set.of(
            "password", "contrasena", "clave",
            "token", "authorization", "bearer",
            "secret", "apikey", "api_key",
            "creditcard", "tarjeta", "cvv",
            "dni", "documento",
            "privatekey", "private_key",
            "accesskey", "access_key",
            "refreshtoken", "refresh_token"
    );

    @Override
    public Map<String, Object> enmascararMapa(Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            return datos;
        }

        Map<String, Object> resultado = new HashMap<>();

        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            String clave = entry.getKey();
            Object valor = entry.getValue();
            resultado.put(clave, enmascararValor(clave, valor));
        }

        return resultado;
    }

    @Override
    public Object enmascararValor(String clave, Object valor) {
        if (clave == null) {
            return valor;
        }

        String claveLower = clave.toLowerCase();
        if (esCampoSensible(claveLower)) {
            return MASCARA;
        }

        if (valor instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapaAnidado = (Map<String, Object>) valor;
            return enmascararMapa(mapaAnidado);
        }

        if (valor instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> lista = (List<Object>) valor;
            return lista.stream()
                    .map(item -> {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapaItem = (Map<String, Object>) item;
                            return enmascararMapa(mapaItem);
                        }
                        return item;
                    })
                    .toList();
        }

        return valor;
    }

    @Override
    public String enmascararTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        String resultado = texto;
        resultado = resultado.replaceAll(
                "(?i)(password|contrasena|clave|token|secret)\\s*[:=]\\s*[^\\s,;]+",
                "$1=" + MASCARA);
        resultado = resultado.replaceAll(
                "(?i)bearer\\s+[A-Za-z0-9\\-._~+/]+=*",
                "Bearer " + MASCARA);
        return resultado;
    }

    private boolean esCampoSensible(String claveLower) {
        return CAMPOS_SENSIBLES.stream().anyMatch(claveLower::contains);
    }
}
