package pe.edu.upeu.saludablemente.actividades.actividad.service;

import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

public interface ActividadService {

    List<ActividadResponse> findAll();

    ActividadResponse findById(Long id);

    ActividadDetalleResponse findDetalleById(Long id);

    List<ActividadResumen> search(EstadoActividad estado, LocalDate desde, LocalDate hasta, Sort sort);

    List<ActividadAgregado> getAggregates(LocalDate desde, LocalDate hasta);

    void validateExists(Long id);

    ActividadResponse create(ActividadRequest request);

    ActividadResponse update(Long id, ActividadRequest request);
}
