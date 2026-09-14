package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service;

import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.DesestimarAlertaRequestDTO;

import java.util.List;

public interface ResolucionService {

    void atenderAlerta(AtenderAlertaRequestDTO request);

    void desestimarAlerta(DesestimarAlertaRequestDTO request);

    List<CatalogoAccionResponseDTO> obtenerCatalogoAcciones();
}
