package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository.RecomendacionIARepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class VigenciaVersionadoService {

    private final RecomendacionIARepository recomendacionRepository;

    @Transactional
    public void desactivarRecomendacionesAnteriores(Long idPersona) {
        log.info(" Desactivando recomendaciones anteriores para persona: {}", idPersona);
        recomendacionRepository.desactivarRecomendacionesVigentes(idPersona);
        log.info(" Recomendaciones anteriores desactivadas para persona: {}", idPersona);
    }
}