package pe.edu.upeu.saludablemente.actividades.actividad.service;

import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;

import java.util.List;

public interface ActividadService {

    List<ActividadResponse> findAll();

    ActividadResponse findById(Long id);

    ActividadDetalleResponse findDetalleById(Long id);

    void validateExists(Long id);

    ActividadResponse create(ActividadRequest request);

    ActividadResponse update(Long id, ActividadRequest request);
}
