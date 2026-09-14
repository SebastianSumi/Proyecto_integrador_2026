package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;

@Slf4j
@Service
public class DataMaskingFilterServiceImpl implements DataMaskingFilterService {

    private static final int PRIVILEGIO_BASICO = 0;
    private static final int PRIVILEGIO_MEDIO = 1;

    @Override
    public CohorteClinicaDTO enmascararParaPreview(CohorteClinicaDTO cohorte, int nivelPrivilegio) {
        if (cohorte == null || cohorte.getFiliacion() == null) {
            return cohorte;
        }

        FiliacionDTO filiacion = cohorte.getFiliacion();

        if (nivelPrivilegio <= PRIVILEGIO_BASICO) {
            filiacion.setNombreCompleto(enmascararNombre(filiacion.getNombreCompleto()));
            filiacion.setCodigoColaborador(enmascararCodigo(filiacion.getCodigoColaborador()));
        } else if (nivelPrivilegio == PRIVILEGIO_MEDIO) {
            filiacion.setNombreCompleto(enmascararParcialNombre(filiacion.getNombreCompleto()));
        }

        return cohorte;
    }

    private String enmascararNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return nombre;
        }
        return "***";
    }

    private String enmascararParcialNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return nombre;
        }
        String[] partes = nombre.split(" ");
        if (partes.length < 2) {
            return nombre.charAt(0) + "***";
        }
        return partes[0] + " " + partes[1].charAt(0) + ".";
    }

    private String enmascararCodigo(String codigo) {
        if (codigo == null || codigo.length() < 4) {
            return "***";
        }
        return codigo.substring(0, 3) + "***" + codigo.substring(codigo.length() - 2);
    }
}
