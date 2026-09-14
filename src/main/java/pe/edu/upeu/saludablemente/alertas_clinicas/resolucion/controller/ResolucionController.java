package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service.ResolucionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resoluciones")
@RequiredArgsConstructor
@Tag(name = "Resolución Clínica", description = "Endpoints para atención formal, desestimación justificada y consulta de catálogo de acciones correctivas.")
public class ResolucionController {

    private final ResolucionService resolucionService;

    @PutMapping("/atender")
    @Operation(summary = "Atender alerta clínica", description = "Cierra la alerta aplicando una acción correctiva estandarizada, notas clínicas y programando seguimiento.")
    public ResponseEntity<Map<String, String>> atenderAlerta(@Valid @RequestBody AtenderAlertaRequestDTO request) {
        resolucionService.atenderAlerta(request);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Alerta atendida con éxito",
                "idAlerta", request.getIdAlerta().toString(),
                "estado", "ATENDIDA"
        ));
    }

    @PutMapping("/desestimar")
    @Operation(summary = "Desestimar alerta clínica", description = "Anula la alerta exigiendo obligatoriamente un motivo clínico estructurado.")
    public ResponseEntity<Map<String, String>> desestimarAlerta(@Valid @RequestBody DesestimarAlertaRequestDTO request) {
        resolucionService.desestimarAlerta(request);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Alerta desestimada con éxito",
                "idAlerta", request.getIdAlerta().toString(),
                "estado", "DESESTIMADA"
        ));
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Catálogo de acciones correctivas", description = "Retorna la lista de intervenciones clínicas tipificadas disponibles para la interfaz del evaluador.")
    public ResponseEntity<List<CatalogoAccionResponseDTO>> obtenerCatalogoAcciones() {
        List<CatalogoAccionResponseDTO> catalogo = resolucionService.obtenerCatalogoAcciones();
        return ResponseEntity.ok(catalogo);
    }
}
