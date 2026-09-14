package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.AlertaPacienteResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AnaliticaLongitudinalDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionClinicaResumenDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionHistoricoItemDTO;

import java.util.List;

public interface AnaliticaLongitudinalService {

    AnaliticaLongitudinalDTO procesarEvolucion(
            EvaluacionClinicaResumenDTO evaluacionActual,
            List<EvaluacionHistoricoItemDTO> evaluacionHistorica);

    String calcularSemaforoGlobal(
            EvaluacionClinicaResumenDTO evaluacionActual,
            List<AlertaPacienteResponseDTO> alertas);
}
