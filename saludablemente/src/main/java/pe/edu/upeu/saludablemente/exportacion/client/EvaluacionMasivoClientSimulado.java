package pe.edu.upeu.saludablemente.exportacion.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.AnaliticaHistoricaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.AntropometriaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.BioquimicaHemodinamicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ComposicionCorporalDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.PuntoHistoricoDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EvaluacionMasivoClientSimulado implements EvaluacionMasivoClient {

    @Override
    public Map<Long, AntropometriaDTO> extraerAntropometria(List<Long> idsPersona) {
        Map<Long, AntropometriaDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            double peso = 70 + (id % 30);
            double talla = 1.60 + ((id % 20) / 100.0);
            double imc = peso / (talla * talla);

            mapa.put(id, AntropometriaDTO.builder()
                    .pesoKg(redondear(peso))
                    .tallaCm(redondear(talla * 100))
                    .imc(redondear(imc))
                    .diagnosticoImc(clasificarImc(imc))
                    .perimetroAbdominalCm(redondear(80 + (id % 20)))
                    .diagnosticoPerimetro("NORMAL")
                    .build());
        }
        return mapa;
    }

    @Override
    public Map<Long, ComposicionCorporalDTO> extraerComposicionCorporal(List<Long> idsPersona) {
        Map<Long, ComposicionCorporalDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            mapa.put(id, ComposicionCorporalDTO.builder()
                    .porcentajeGrasa(redondear(20 + (id % 15)))
                    .diagnosticoGrasa("ELEVADO")
                    .porcentajeMusculo(redondear(30 + (id % 10)))
                    .diagnosticoMusculo("NORMAL")
                    .nivelGrasaVisceral(redondear(8 + (id % 6)))
                    .diagnosticoGrasaVisceral("ELEVADO_LEVE")
                    .build());
        }
        return mapa;
    }

    @Override
    public Map<Long, BioquimicaHemodinamicaDTO> extraerBioquimica(List<Long> idsPersona) {
        Map<Long, BioquimicaHemodinamicaDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            mapa.put(id, BioquimicaHemodinamicaDTO.builder()
                    .glucosaMgDl(redondear(90 + (id % 40)))
                    .colesterolTotalMgDl(redondear(180 + (id % 50)))
                    .colesterolHdlMgDl(redondear(40 + (id % 20)))
                    .colesterolLdlMgDl(redondear(100 + (id % 40)))
                    .trigliceridosMgDl(redondear(120 + (id % 80)))
                    .presionSistolica(115 + (int) (id % 20))
                    .presionDiastolica(75 + (int) (id % 15))
                    .build());
        }
        return mapa;
    }

    @Override
    public Map<Long, AnaliticaHistoricaDTO> extraerAnaliticaHistorica(List<Long> idsPersona) {
        Map<Long, AnaliticaHistoricaDTO> mapa = new HashMap<>();
        for (Long id : idsPersona) {
            List<PuntoHistoricoDTO> puntos = new ArrayList<>();
            puntos.add(PuntoHistoricoDTO.builder()
                    .periodo("2025-2")
                    .imc(redondear(29.5 + (id % 3)))
                    .grasaVisceral(redondear(12 + (id % 3)))
                    .glucosaMgDl(redondear(115 + (id % 10)))
                    .colesterolTotalMgDl(redondear(210 + (id % 20)))
                    .build());
            puntos.add(PuntoHistoricoDTO.builder()
                    .periodo("2026-1")
                    .imc(redondear(29.0 + (id % 3)))
                    .grasaVisceral(redondear(11 + (id % 3)))
                    .glucosaMgDl(redondear(110 + (id % 10)))
                    .colesterolTotalMgDl(redondear(205 + (id % 20)))
                    .build());
            puntos.add(PuntoHistoricoDTO.builder()
                    .periodo("2026-2")
                    .imc(redondear(28.5 + (id % 3)))
                    .grasaVisceral(redondear(10 + (id % 3)))
                    .glucosaMgDl(redondear(105 + (id % 10)))
                    .colesterolTotalMgDl(redondear(200 + (id % 20)))
                    .build());

            mapa.put(id, AnaliticaHistoricaDTO.builder()
                    .deltaImcSemestreAnterior(-0.45)
                    .deltaGrasaVisceralSemestreAnterior(-1.0)
                    .tendenciaMetabolica("MEJORA_LEVE")
                    .puntosHistoricos(puntos)
                    .semaforoBienestarGlobal("OBSERVACION")
                    .build());
        }
        return mapa;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String clasificarImc(double imc) {
        if (imc < 18.5) return "BAJO_PESO";
        if (imc < 25.0) return "NORMAL";
        if (imc < 30.0) return "SOBREPESO";
        return "OBESIDAD";
    }
}
