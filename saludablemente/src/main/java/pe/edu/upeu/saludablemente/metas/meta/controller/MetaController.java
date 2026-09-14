package pe.edu.upeu.saludablemente.metas.meta.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.service.MetaService;

@RestController
@RequestMapping("/api/v1/metas")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService service;

    @GetMapping
    public List<MetaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/persona/{personaId}")
    public List<MetaResponse> findByPersonaId(@PathVariable Long personaId) {
        return service.findByPersonaId(personaId);
    }

    @GetMapping("/{id}")
    public MetaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetaResponse create(@Valid @RequestBody MetaCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MetaResponse update(@PathVariable Long id, @Valid @RequestBody MetaUpdateRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/cumplimiento")
    public MetaResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
