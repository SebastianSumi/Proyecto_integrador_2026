package pe.edu.upeu.saludablemente.exportacion.serializer;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ErrorSerializacionDatasetException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Slf4j
@Component
public class CsvStreamingWriter implements SerializadorFormato {

    private static final char DELIMITADOR = ',';

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.CSV_TABULAR;
    }

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream outputStream) {
        long inicio = System.currentTimeMillis();
        int totalRegistros = 0;

        try {
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            try (CSVWriter csvWriter = new CSVWriter(writer, DELIMITADOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                csvWriter.writeNext(construirCabeceras());

                var iterator = datos.iterator();
                while (iterator.hasNext()) {
                    CohorteClinicaDTO cohorte = iterator.next();
                    csvWriter.writeNext(construirFila(cohorte));
                    totalRegistros++;

                    if (totalRegistros % 5000 == 0) {
                        csvWriter.flush();
                        log.debug("CSV: {} registros escritos", totalRegistros);
                    }
                }

                csvWriter.flush();
            }

            long tiempoMs = System.currentTimeMillis() - inicio;
            log.info("CSV generado. {} registros en {} ms", totalRegistros, tiempoMs);

            return ResultadoSerializacionHelper.construir(
                    FormatoSalida.CSV_TABULAR, totalRegistros, tiempoMs, null);

        } catch (Exception e) {
            log.error("Error al generar CSV: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("Error al generar CSV", e);
        }
    }

    private String[] construirCabeceras() {
        return new String[]{
                "CODIGO_COLABORADOR", "NOMBRE_COMPLETO", "AREA", "SEDE", "EDAD", "SEXO",
                "PESO_KG", "TALLA_CM", "IMC", "DX_IMC", "PERIMETRO_ABD_CM", "DX_PERIMETRO",
                "PORCENTAJE_GRASA", "DX_GRASA", "PORCENTAJE_MUSCULO",
                "GRASA_VISCERAL", "DX_GRASA_VISCERAL",
                "GLUCOSA_MGDL", "COLESTEROL_MGDL", "TRIGLICERIDOS_MGDL",
                "PRESION_SISTOLICA", "PRESION_DIASTOLICA",
                "APTT_ABDOMINALES", "APTT_PLANCHAS", "APTT_SALTO_CM",
                "APTT_CARRERA_400M", "APTT_CLASIFICACION",
                "TOTAL_ALERTAS_ACTIVAS",
                "ENFOQUE_EJERCICIO", "FRECUENCIA_EJERCICIO",
                "ESTRATEGIA_NUTRICIONAL",
                "TOTAL_METAS",
                "TOTAL_TALLERES_ASISTIDOS"
        };
    }

    private String[] construirFila(CohorteClinicaDTO c) {
        return new String[]{
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getCodigoColaborador() : null),
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getNombreCompleto() : null),
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getAreaTrabajo() : null),
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getSede() : null),
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getEdad() : null),
                valorSeguro(c.getFiliacion() != null ? c.getFiliacion().getSexo() : null),

                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getPesoKg() : null),
                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getTallaCm() : null),
                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getImc() : null),
                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getDiagnosticoImc() : null),
                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getPerimetroAbdominalCm() : null),
                valorSeguro(c.getAntropometria() != null ? c.getAntropometria().getDiagnosticoPerimetro() : null),

                valorSeguro(c.getComposicionCorporal() != null ? c.getComposicionCorporal().getPorcentajeGrasa() : null),
                valorSeguro(c.getComposicionCorporal() != null ? c.getComposicionCorporal().getDiagnosticoGrasa() : null),
                valorSeguro(c.getComposicionCorporal() != null ? c.getComposicionCorporal().getPorcentajeMusculo() : null),
                valorSeguro(c.getComposicionCorporal() != null ? c.getComposicionCorporal().getNivelGrasaVisceral() : null),
                valorSeguro(c.getComposicionCorporal() != null ? c.getComposicionCorporal().getDiagnosticoGrasaVisceral() : null),

                valorSeguro(c.getBioquimica() != null ? c.getBioquimica().getGlucosaMgDl() : null),
                valorSeguro(c.getBioquimica() != null ? c.getBioquimica().getColesterolTotalMgDl() : null),
                valorSeguro(c.getBioquimica() != null ? c.getBioquimica().getTrigliceridosMgDl() : null),
                valorSeguro(c.getBioquimica() != null ? c.getBioquimica().getPresionSistolica() : null),
                valorSeguro(c.getBioquimica() != null ? c.getBioquimica().getPresionDiastolica() : null),

                valorSeguro(c.getAptitudFisica() != null ? c.getAptitudFisica().getAbdominales1Min() : null),
                valorSeguro(c.getAptitudFisica() != null ? c.getAptitudFisica().getPlanchas1Min() : null),
                valorSeguro(c.getAptitudFisica() != null ? c.getAptitudFisica().getSaltoSinImpulsoCm() : null),
                valorSeguro(c.getAptitudFisica() != null ? c.getAptitudFisica().getCarrera400mSegundos() : null),
                valorSeguro(c.getAptitudFisica() != null ? c.getAptitudFisica().getNivelAptitud() : null),

                c.getAlertas() != null ? String.valueOf(c.getAlertas().size()) : "0",

                valorSeguro(c.getRecomendacionIA() != null ? c.getRecomendacionIA().getEnfoquePrincipal() : null),
                valorSeguro(c.getRecomendacionIA() != null ? c.getRecomendacionIA().getFrecuenciaSemanalDias() : null),
                valorSeguro(c.getRecomendacionIA() != null ? c.getRecomendacionIA().getEstrategiaNutricional() : null),

                c.getMetas() != null ? String.valueOf(c.getMetas().size()) : "0",
                c.getTalleresAsistidos() != null ? String.valueOf(c.getTalleresAsistidos().size()) : "0"
        };
    }

    private String valorSeguro(Object valor) {
        if (valor == null) return "";
        if (valor instanceof BigDecimal bd) return bd.toPlainString();
        return valor.toString();
    }
}
