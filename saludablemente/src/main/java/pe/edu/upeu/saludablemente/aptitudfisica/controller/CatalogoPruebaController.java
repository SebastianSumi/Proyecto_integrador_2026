package pe.edu.upeu.saludablemente.aptitudfisica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.service.CatalogoPruebaService;

import java.util.List;

@Tag(name = "Catalogo Pruebas")
@RestController
@RequestMapping("/api/v1/catalogo-pruebas")
@RequiredArgsConstructor
public class CatalogoPruebaController {

    private final CatalogoPruebaService catalogoPruebaService;

    @Operation(summary = "Lista el catalogo de pruebas fisicas (por defecto, solo activos)")
    @GetMapping
    public ResponseEntity<List<CatalogoPruebaDto>> listar(
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(catalogoPruebaService.listar(soloActivos));
    }

    @Operation(summary = "Consulta una prueba del catalogo por id")
    @GetMapping("/{id}")
    public ResponseEntity<CatalogoPruebaDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoPruebaService.obtener(id));
    }

    @Operation(summary = "Registra una nueva prueba en el catalogo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoPruebaDto crear(@Valid @RequestBody CatalogoPruebaDto request) {
        return catalogoPruebaService.crear(request);
    }

    @Operation(summary = "Actualiza una prueba del catalogo")
    @PutMapping("/{id}")
    public ResponseEntity<CatalogoPruebaDto> actualizar(
            @PathVariable Long id, @Valid @RequestBody CatalogoPruebaDto request) {
        return ResponseEntity.ok(catalogoPruebaService.actualizar(id, request));
    }

    @Operation(summary = "Elimina una prueba del catalogo")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        catalogoPruebaService.eliminar(id);
    }
}