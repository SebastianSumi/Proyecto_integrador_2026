package pe.edu.upeu.saludablemente.perfil_reporte.service;

import java.util.List;

public interface ChartRendererService {

    String generarRadarChartSvg(String titulo, List<String> etiquetas, List<Double> valores);

    String generarBarChartSvg(String titulo, List<String> etiquetas, List<Double> valores);
}
