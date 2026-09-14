package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.saludablemente.exception.PoblacionVaciaExportacionException;
import pe.edu.upeu.saludablemente.exportacion.client.ActividadesMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.AlertasMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.AptitudFisicaMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.EvaluacionMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.MetasMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.PersonalMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.client.RecomendacionesMasivoClient;
import pe.edu.upeu.saludablemente.exportacion.dto.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtraccionPoblacionalServiceImpl implements ExtraccionPoblacionalService {

    @Value("${exportacion.chunk.tamano-lote:1000}")
    private int tamanoLote;

    private final PersonalMasivoClient personalClient;
    private final EvaluacionMasivoClient evaluacionClient;
    private final AptitudFisicaMasivoClient aptitudFisicaClient;
    private final AlertasMasivoClient alertasClient;
    private final RecomendacionesMasivoClient recomendacionesClient;
    private final MetasMasivoClient metasClient;
    private final ActividadesMasivoClient actividadesClient;

    private final CursorPaginationProvider cursorProvider;

    @Override
    @Transactional(readOnly = true)
    public long contarPoblacion(FiltroPoblacionalParams filtros) {
        long total = personalClient.contarPoblacion(filtros);
        log.info("Poblacion filtrada: {} colaboradores", total);

        if (total == 0) {
            throw new PoblacionVaciaExportacionException(
                    "Los filtros aplicados no retornan ningun colaborador");
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public Stream<CohorteClinicaDTO> extraerPoblacionStream(FiltroPoblacionalParams filtros) {
        log.info("Iniciando extraccion poblacional: chunk={}, filtros={}", tamanoLote, filtros);

        return cursorProvider.iterarIdsPaginados(filtros, tamanoLote)
                .flatMap(this::ensamblarBloque);
    }

    private Stream<CohorteClinicaDTO> ensamblarBloque(List<Long> bloqueIds) {
        log.debug("Ensamblando bloque de {} colaboradores", bloqueIds.size());

        List<FiliacionDTO> filiaciones = personalClient.extraerFiliacion(bloqueIds);

        Map<Long, AntropometriaDTO> antropometria =
                evaluacionClient.extraerAntropometria(bloqueIds);
        Map<Long, ComposicionCorporalDTO> composicion =
                evaluacionClient.extraerComposicionCorporal(bloqueIds);
        Map<Long, BioquimicaHemodinamicaDTO> bioquimica =
                evaluacionClient.extraerBioquimica(bloqueIds);
        Map<Long, AnaliticaHistoricaDTO> analitica =
                evaluacionClient.extraerAnaliticaHistorica(bloqueIds);

        Map<Long, AptitudFisicaDTO> aptitud =
                aptitudFisicaClient.extraerPruebasFisicas(bloqueIds);
        Map<Long, List<AlertaActivaDTO>> alertas =
                alertasClient.extraerAlertasActivas(bloqueIds);
        Map<Long, RecomendacionVigenteDTO> recomendaciones =
                recomendacionesClient.extraerRecomendacionVigente(bloqueIds);
        Map<Long, List<MetaBienestarDTO>> metas =
                metasClient.extraerMetasActivas(bloqueIds);
        Map<Long, List<ActividadAgendaDTO>> actividades =
                actividadesClient.extraerActividadesAsistidas(bloqueIds);

        return filiaciones.stream()
                .map(fil -> CohorteClinicaDTO.builder()
                        .filiacion(fil)
                        .antropometria(antropometria.get(fil.getIdPersona()))
                        .composicionCorporal(composicion.get(fil.getIdPersona()))
                        .bioquimica(bioquimica.get(fil.getIdPersona()))
                        .aptitudFisica(aptitud.get(fil.getIdPersona()))
                        .analitica(analitica.get(fil.getIdPersona()))
                        .alertas(alertas.getOrDefault(fil.getIdPersona(), List.of()))
                        .recomendacionIA(recomendaciones.get(fil.getIdPersona()))
                        .metas(metas.getOrDefault(fil.getIdPersona(), List.of()))
                        .talleresAsistidos(actividades.getOrDefault(fil.getIdPersona(), List.of()))
                        .build());
    }
}
