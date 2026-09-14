package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.BloqueEjercicioDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PlanNutricionalDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.RecomendacionVigenteDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.RutinaEjercicioDTO;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RecomendacionClient {

    public RecomendacionVigenteDTO obtenerVigente(Long idPersona) {
        log.debug("Obteniendo recomendacion vigente para persona {}", idPersona);
        return RecomendacionVigenteDTO.builder()
                .idRecomendacion(UUID.randomUUID())
                .vigente(true)
                .rutinaEjercicio(RutinaEjercicioDTO.builder()
                        .enfoquePrincipal("Acondicionamiento Cardiovascular")
                        .frecuenciaSemanalDias(3)
                        .bloques(List.of(
                                BloqueEjercicioDTO.builder()
                                        .tipo("Cardiovascular")
                                        .duracionMinutos(30)
                                        .intensidad("Moderada")
                                        .descripcion("Caminata rapida o bicicleta estatica")
                                        .build()))
                        .contraindicaciones(List.of("Evitar cargas maximas por encima de la cabeza"))
                        .build())
                .planNutricional(PlanNutricionalDTO.builder()
                        .estrategiaGeneral("Control glucemico y balance hidroelectrolitico")
                        .pautasClave(List.of(
                                "Consumir al menos 2 litros de agua",
                                "Distribuir comidas en 3 tiempos principales y 1 colacion",
                                "Priorizar alimentos ricos en potasio y fibra"))
                        .alimentosPrioritarios(List.of("Avena integral", "Lentejas", "Pescado blanco"))
                        .alimentosAReducir(List.of("Bebidas azucaradas", "Snacks ultraprocesados"))
                        .build())
                .build();
    }
}
