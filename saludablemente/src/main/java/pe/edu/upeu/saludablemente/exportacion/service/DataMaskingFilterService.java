package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;

public interface DataMaskingFilterService {

    CohorteClinicaDTO enmascararParaPreview(CohorteClinicaDTO cohorte, int nivelPrivilegio);
}
