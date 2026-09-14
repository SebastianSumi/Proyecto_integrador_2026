package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChartRendererServiceImpl implements ChartRendererService {

    private static final int ANCHO = 400;
    private static final int ALTO = 400;
    private static final int CENTRO_X = ANCHO / 2;
    private static final int CENTRO_Y = ALTO / 2;
    private static final int RADIO = 150;

    @Override
    public String generarRadarChartSvg(String titulo, List<String> etiquetas, List<Double> valores) {
        if (etiquetas == null || etiquetas.isEmpty()) {
            return "";
        }
        int numEjes = etiquetas.size();
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(ANCHO)
           .append("\" height=\"").append(ALTO).append("\" viewBox=\"0 0 ")
           .append(ANCHO).append(" ").append(ALTO).append("\">");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");

        for (int i = 0; i < numEjes; i++) {
            double angulo = (2 * Math.PI * i) / numEjes - Math.PI / 2;
            int x = (int) (CENTRO_X + RADIO * Math.cos(angulo));
            int y = (int) (CENTRO_Y + RADIO * Math.sin(angulo));
            svg.append("<line x1=\"").append(CENTRO_X).append("\" y1=\"").append(CENTRO_Y)
               .append("\" x2=\"").append(x).append("\" y2=\"").append(y)
               .append("\" stroke=\"#cccccc\" stroke-width=\"1\"/>");
        }
        for (int nivel = 1; nivel <= 5; nivel++) {
            int radioNivel = RADIO * nivel / 5;
            svg.append("<circle cx=\"").append(CENTRO_X).append("\" cy=\"").append(CENTRO_Y)
               .append("\" r=\"").append(radioNivel)
               .append("\" fill=\"none\" stroke=\"#eeeeee\" stroke-width=\"1\"/>");
        }
        if (valores != null && valores.size() == numEjes) {
            StringBuilder puntos = new StringBuilder();
            for (int i = 0; i < numEjes; i++) {
                double factor = Math.min(valores.get(i) / 100.0, 1.0);
                double angulo = (2 * Math.PI * i) / numEjes - Math.PI / 2;
                int x = (int) (CENTRO_X + RADIO * factor * Math.cos(angulo));
                int y = (int) (CENTRO_Y + RADIO * factor * Math.sin(angulo));
                if (i > 0) puntos.append(" ");
                puntos.append(x).append(",").append(y);
            }
            svg.append("<polygon points=\"").append(puntos)
               .append("\" fill=\"rgba(52,152,219,0.3)\" stroke=\"#3498db\" stroke-width=\"2\"/>");
        }
        for (int i = 0; i < numEjes; i++) {
            double angulo = (2 * Math.PI * i) / numEjes - Math.PI / 2;
            int x = (int) (CENTRO_X + (RADIO + 30) * Math.cos(angulo));
            int y = (int) (CENTRO_Y + (RADIO + 30) * Math.sin(angulo));
            svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
               .append("\" font-size=\"12\" text-anchor=\"middle\" fill=\"#333333\">")
               .append(etiquetas.get(i)).append("</text>");
        }
        svg.append("</svg>");
        return svg.toString();
    }

    @Override
    public String generarBarChartSvg(String titulo, List<String> etiquetas, List<Double> valores) {
        if (etiquetas == null || etiquetas.isEmpty()) {
            return "";
        }
        int anchoBarra = 60;
        int espacio = 40;
        int margenIzquierdo = 60;
        int margenSuperior = 40;
        int margenInferior = 60;
        int altoGrafico = 300;
        int anchoTotal = margenIzquierdo + etiquetas.size() * (anchoBarra + espacio) + espacio;
        int altoTotal = margenSuperior + altoGrafico + margenInferior;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(anchoTotal)
           .append("\" height=\"").append(altoTotal).append("\" viewBox=\"0 0 ")
           .append(anchoTotal).append(" ").append(altoTotal).append("\">");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");
        svg.append("<line x1=\"").append(margenIzquierdo).append("\" y1=\"").append(margenSuperior)
           .append("\" x2=\"").append(margenIzquierdo).append("\" y2=\"").append(margenSuperior + altoGrafico)
           .append("\" stroke=\"#333333\" stroke-width=\"2\"/>");
        svg.append("<line x1=\"").append(margenIzquierdo).append("\" y1=\"").append(margenSuperior + altoGrafico)
           .append("\" x2=\"").append(anchoTotal - espacio).append("\" y2=\"").append(margenSuperior + altoGrafico)
           .append("\" stroke=\"#333333\" stroke-width=\"2\"/>");

        if (valores != null) {
            double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(100.0);
            if (maxValor <= 0) maxValor = 100.0;
            for (int i = 0; i < valores.size() && i < etiquetas.size(); i++) {
                double valor = valores.get(i);
                int alturaBarra = (int) ((valor / maxValor) * altoGrafico);
                int x = margenIzquierdo + espacio + i * (anchoBarra + espacio);
                int y = margenSuperior + altoGrafico - alturaBarra;

                svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                   .append("\" width=\"").append(anchoBarra).append("\" height=\"").append(alturaBarra)
                   .append("\" fill=\"#3498db\"/>");
                svg.append("<text x=\"").append(x + anchoBarra / 2).append("\" y=\"").append(y - 5)
                   .append("\" font-size=\"11\" text-anchor=\"middle\" fill=\"#333333\">")
                   .append(String.format("%.1f", valor)).append("</text>");
                svg.append("<text x=\"").append(x + anchoBarra / 2).append("\" y=\"").append(margenSuperior + altoGrafico + 20)
                   .append("\" font-size=\"11\" text-anchor=\"middle\" fill=\"#333333\">")
                   .append(etiquetas.get(i)).append("</text>");
            }
        }
        svg.append("</svg>");
        return svg.toString();
    }
}
