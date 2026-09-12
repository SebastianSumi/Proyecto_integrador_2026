package pe.edu.upeu.saludablemente.actividades.actividad.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;

@RestController
@RequestMapping("/api/v1/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService service;

    @GetMapping
    public List<ActividadResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ActividadResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActividadResponse create(@Valid @RequestBody ActividadRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ActividadResponse update(@PathVariable Long id, @Valid @RequestBody ActividadRequest request) {
        return service.update(id, request);
    }
}
