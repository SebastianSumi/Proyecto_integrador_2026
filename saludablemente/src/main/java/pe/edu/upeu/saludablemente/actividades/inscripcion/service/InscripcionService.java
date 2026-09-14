package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionRequest;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionResponse;

import java.util.List;

public interface InscripcionService {

    List<InscripcionResponse> findAll();

    InscripcionResponse findById(Long id);

    InscripcionResponse register(InscripcionRequest request);

    InscripcionResponse cancel(Long id);
}
