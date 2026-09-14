package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.NivelPrivacidadAplicadoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KAnonymityEngine {

    private static final int K_UMBRAL = 5;
    private static final int RANGO_EDAD = 5;

    public record ResultadoAnonimizacion(List<CohorteClinicaDTO> cohortes, NivelPrivacidadAplicadoDTO metadatos) {}

    public ResultadoAnonimizacion anonimizar(List<CohorteClinicaDTO> cohortes) {
        log.info("Aplicando K-Anonymity sobre {} colaboradores (k={})", cohortes.size(), K_UMBRAL);

        List<CohorteClinicaDTO> generalizados = new ArrayList<>();
        for (CohorteClinicaDTO cohorte : cohortes) {
            CohorteClinicaDTO procesado = suprimirIdentificadoresDirectos(cohorte);
            procesado = generalizarCuasiIdentificadores(procesado);
            generalizados.add(procesado);
        }

        Map<String, Long> frecuencias = generalizados.stream()
                .collect(Collectors.groupingBy(this::construirClaveCuasiIdentificadores, Collectors.counting()));

        List<CohorteClinicaDTO> filtrados = new ArrayList<>();
        int suprimidos = 0;

        for (CohorteClinicaDTO c : generalizados) {
            String clave = construirClaveCuasiIdentificadores(c);
            long frecuencia = frecuencias.getOrDefault(clave, 0L);

            if (frecuencia >= K_UMBRAL) {
                filtrados.add(c);
            } else {
                suprimidos++;
            }
        }

        log.info("K-Anonymity finalizado. Originales: {}, Resultantes: {}, Suprimidos: {}",
                cohortes.size(), filtrados.size(), suprimidos);

        NivelPrivacidadAplicadoDTO metadatos = NivelPrivacidadAplicadoDTO.builder()
                .modoPrivacidad("ANONIMIZADO_ESTADISTICO")
                .identificadoresSuprimidos(true)
                .cuasiIdentificadoresGeneralizados(true)
                .registrosSuprimidosPorKAnonimity(suprimidos)
                .kAnonimityAplicado(K_UMBRAL)
                .build();

        return new ResultadoAnonimizacion(filtrados, metadatos);
    }

    private CohorteClinicaDTO suprimirIdentificadoresDirectos(CohorteClinicaDTO c) {
        if (c.getFiliacion() == null) return c;
        FiliacionDTO f = c.getFiliacion();
        f.setNombreCompleto(null);
        f.setCodigoColaborador(null);
        return c;
    }

    private CohorteClinicaDTO generalizarCuasiIdentificadores(CohorteClinicaDTO c) {
        if (c.getFiliacion() == null) return c;
        FiliacionDTO f = c.getFiliacion();
        if (f.getEdad() != null) {
            f.setEdad((f.getEdad() / RANGO_EDAD) * RANGO_EDAD);
        }
        return c;
    }

    private String construirClaveCuasiIdentificadores(CohorteClinicaDTO c) {
        if (c.getFiliacion() == null) return "SIN_DATOS";
        FiliacionDTO f = c.getFiliacion();
        return String.format("%s|%s|%s",
                f.getEdad() != null ? f.getEdad() : "NA",
                f.getSexo() != null ? f.getSexo() : "NA",
                f.getSede() != null ? f.getSede() : "NA");
    }
}
