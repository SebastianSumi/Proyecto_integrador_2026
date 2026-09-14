package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class CodebookGenerator {

    public void generarCodebookHtml(String tituloLote, String periodoSemestral,
                                    String usuario, OutputStream outputStream) {
        try {
            String html = construirHtml(tituloLote, periodoSemestral, usuario);
            outputStream.write(html.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            log.info("Codebook HTML generado");
        } catch (Exception e) {
            log.error("Error al generar Codebook: {}", e.getMessage(), e);
        }
    }

    private String construirHtml(String tituloLote, String periodoSemestral, String usuario) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>")
          .append("<html lang='es'><head><meta charset='UTF-8'>")
          .append("<title>Diccionario de Variables - Exportacion</title>")
          .append("<style>")
          .append("body{font-family:Arial,sans-serif;font-size:12px;color:#333;margin:20px;}")
          .append("h1{color:#1e5631;border-bottom:3px solid #1e5631;padding-bottom:8px;}")
          .append("h2{color:#2c7a47;margin-top:24px;}")
          .append("table{width:100%;border-collapse:collapse;margin:12px 0;}")
          .append("th{background:#1e5631;color:white;padding:8px;text-align:left;font-size:11px;}")
          .append("td{border:1px solid #ddd;padding:6px;font-size:11px;}")
          .append("tr:nth-child(even){background:#f6f6f6;}")
          .append(".meta{background:#f0f7f0;padding:12px;border-left:4px solid #1e5631;margin:16px 0;}")
          .append("code{background:#eee;padding:2px 6px;border-radius:3px;font-size:10px;}")
          .append("</style></head><body>");

        sb.append("<h1>Diccionario de Variables - Exportacion de Datos</h1>");

        sb.append("<div class='meta'>")
          .append("<p><strong>Titulo del lote:</strong> ").append(safe(tituloLote)).append("</p>")
          .append("<p><strong>Periodo semestral:</strong> ").append(safe(periodoSemestral)).append("</p>")
          .append("<p><strong>Usuario solicitante:</strong> ").append(safe(usuario)).append("</p>")
          .append("<p><strong>Fecha de generacion:</strong> ").append(LocalDateTime.now()).append("</p>")
          .append("</div>");

        sb.append("<p>Este documento describe cada variable incluida en el paquete de exportacion. Utilice esta referencia para interpretar correctamente los datos.</p>");

        agregarSeccion(sb, "1. Filiacion del Colaborador", List.of(
                new Var("codigoColaborador", "Codigo unico del colaborador", "Texto", "-", "Personal"),
                new Var("nombreCompleto", "Nombres y apellidos", "Texto", "-", "Personal"),
                new Var("areaTrabajo", "Area o departamento", "Texto", "-", "Personal"),
                new Var("sede", "Sede institucional", "Texto", "-", "Personal"),
                new Var("edad", "Edad calculada", "Numero entero", "18-65", "Personal"),
                new Var("sexo", "Sexo biologico", "Texto", "M/F", "Personal")
        ));

        agregarSeccion(sb, "2. Antropometria", List.of(
                new Var("pesoKg", "Peso corporal", "Decimal", "kg", "Evaluacion Nutricional"),
                new Var("tallaCm", "Talla o estatura", "Decimal", "cm", "Evaluacion Nutricional"),
                new Var("imc", "Indice de Masa Corporal", "Decimal", "18.5-24.9 kg/m2", "Evaluacion Nutricional"),
                new Var("diagnosticoImc", "Diagnostico IMC", "Texto", "Normal/Sobrepeso/Obesidad", "Evaluacion Nutricional"),
                new Var("perimetroAbdominalCm", "Perimetro abdominal", "Decimal", "cm", "Evaluacion Nutricional")
        ));

        agregarSeccion(sb, "3. Composicion Corporal", List.of(
                new Var("porcentajeGrasa", "Porcentaje de grasa corporal", "Decimal", "%", "Evaluacion Nutricional"),
                new Var("porcentajeMusculo", "Porcentaje de masa muscular", "Decimal", "%", "Evaluacion Nutricional"),
                new Var("nivelGrasaVisceral", "Nivel de grasa visceral", "Decimal", "<=9 normal", "Evaluacion Nutricional")
        ));

        agregarSeccion(sb, "4. Bioquimica", List.of(
                new Var("glucosaMgDl", "Glucosa basal", "Decimal", "70-100 mg/dL", "Laboratorio"),
                new Var("colesterolTotalMgDl", "Colesterol total", "Decimal", "<200 mg/dL", "Laboratorio"),
                new Var("colesterolHdlMgDl", "Colesterol HDL", "Decimal", ">40 mg/dL", "Laboratorio"),
                new Var("colesterolLdlMgDl", "Colesterol LDL", "Decimal", "<130 mg/dL", "Laboratorio"),
                new Var("trigliceridosMgDl", "Trigliceridos", "Decimal", "<150 mg/dL", "Laboratorio"),
                new Var("presionSistolica", "Presion arterial sistolica", "Entero", "<120 mmHg", "Evaluacion"),
                new Var("presionDiastolica", "Presion arterial diastolica", "Entero", "<80 mmHg", "Evaluacion")
        ));

        agregarSeccion(sb, "5. Aptitud Fisica", List.of(
                new Var("abdominales1Min", "Abdominales en 1 minuto", "Entero", "reps", "Aptitud Fisica"),
                new Var("planchas1Min", "Planchas en 1 minuto", "Entero", "reps", "Aptitud Fisica"),
                new Var("saltoSinImpulsoCm", "Salto sin impulso", "Decimal", "cm", "Aptitud Fisica"),
                new Var("carrera400mSegundos", "Tiempo en carrera 400m", "Entero", "segundos", "Aptitud Fisica")
        ));

        agregarSeccion(sb, "6. Alertas Clinicas", List.of(
                new Var("tipoIndicador", "Indicador clinico afectado", "Texto", "-", "Alertas Clinicas"),
                new Var("severidad", "Severidad de la alerta", "Texto", "INFO/WARNING/CRITICAL", "Alertas Clinicas"),
                new Var("estado", "Estado de atencion", "Texto", "PENDIENTE/EN_REVISION/RESUELTO", "Alertas Clinicas"),
                new Var("mensajeAmigable", "Mensaje descriptivo para el colaborador", "Texto", "-", "Alertas Clinicas")
        ));

        agregarSeccion(sb, "7. Recomendaciones IA", List.of(
                new Var("enfoquePrincipal", "Enfoque principal del plan", "Texto", "-", "Recomendaciones IA"),
                new Var("frecuenciaSemanalDias", "Dias de entrenamiento por semana", "Entero", "dias", "Recomendaciones IA"),
                new Var("estrategiaNutricional", "Estrategia nutricional sugerida", "Texto", "-", "Recomendaciones IA")
        ));

        sb.append("<hr style='margin-top:40px;'>")
          .append("<p style='font-size:10px;color:#777;text-align:center;'>")
          .append("Documento generado automaticamente por el modulo de Exportacion. ")
          .append("Sistema Saludablemente - UPeU</p>")
          .append("</body></html>");

        return sb.toString();
    }

    private void agregarSeccion(StringBuilder sb, String titulo, List<Var> variables) {
        sb.append("<h2>").append(titulo).append("</h2>");
        sb.append("<table><thead><tr>")
          .append("<th>Variable</th><th>Descripcion</th><th>Tipo</th>")
          .append("<th>Unidad / Rango</th><th>Modulo Origen</th>")
          .append("</tr></thead><tbody>");

        for (Var v : variables) {
            sb.append("<tr>")
              .append("<td><code>").append(v.nombre).append("</code></td>")
              .append("<td>").append(v.descripcion).append("</td>")
              .append("<td>").append(v.tipo).append("</td>")
              .append("<td>").append(v.unidad).append("</td>")
              .append("<td>").append(v.origen).append("</td>")
              .append("</tr>");
        }

        sb.append("</tbody></table>");
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private record Var(String nombre, String descripcion, String tipo, String unidad, String origen) {
    }
}
