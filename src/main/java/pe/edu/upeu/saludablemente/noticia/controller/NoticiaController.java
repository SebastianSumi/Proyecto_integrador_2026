package pe.edu.upeu.saludablemente.noticia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.noticia.dto.NoticiaDto;
import pe.edu.upeu.saludablemente.noticia.service.NoticiaService;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
@CrossOrigin(origins = "*")
public class NoticiaController {

    @Autowired
    private NoticiaService noticiaService;

    @GetMapping
    public ResponseEntity<List<NoticiaDto>> listarTodas() {
        return ResponseEntity.ok(noticiaService.listarTodas());
    }

    @GetMapping("/publicadas")
    public ResponseEntity<List<NoticiaDto>> listarPublicadas() {
        return ResponseEntity.ok(noticiaService.listarPublicadas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticiaDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<NoticiaDto> crear(@RequestBody NoticiaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noticiaService.crearNoticia(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticiaDto> actualizar(@PathVariable Long id, @RequestBody NoticiaDto dto) {
        return ResponseEntity.ok(noticiaService.actualizarNoticia(id, dto));
    }

    @PatchMapping("/{id}/despublicar")
    public ResponseEntity<NoticiaDto> despublicar(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.despublicarNoticia(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        noticiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
