package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto.ConfirmarLecturaRecomendacionRequestDTO;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto.RecomendacionVigenteResponseDTO;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.service.RecomendacionTransaccionalService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/recomendaciones/colaborador")
@RequiredArgsConstructor
@Tag(name = "Recomendaciones IA - Colaborador", description = "API para colaboradores")
public class RecomendacionColaboradorController {

    private final RecomendacionTransaccionalService recomendacionService;

    @GetMapping("/vigente")
    @Operation(summary = "Obtener recomendación vigente para el colaborador")
    public ResponseEntity<RecomendacionVigenteResponseDTO> obtenerRecomendacionVigente(
            @RequestParam Long idPersona) {

        log.info(" Consultando recomendación vigente para persona: {}", idPersona);
        RecomendacionVigenteResponseDTO recomendacion = recomendacionService
                .obtenerRecomendacionVigente(idPersona);

        return ResponseEntity.ok(recomendacion);
    }

    @PostMapping("/confirmar-lectura")
    @Operation(summary = "Confirmar lectura de la recomendación")
    public ResponseEntity<Map<String, Object>> confirmarLectura(
            @Valid @RequestBody ConfirmarLecturaRecomendacionRequestDTO request) {

        log.info(" Confirmando lectura: {}", request.getIdRecomendacion());
        recomendacionService.confirmarLectura(request.getIdRecomendacion());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Recomendación marcada como leída exitosamente");
        response.put("idRecomendacion", request.getIdRecomendacion());

        return ResponseEntity.ok(response);
    }
}