package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenEjecutivoDTO {
    private String tituloLote;
    private String periodoSemestral;
    private String fechaGeneracion;
    private long totalColaboradores;
    private int edadPromedio;
    private int totalMasculino;
    private int totalFemenino;
    private Map<String, Long> distribucionPorRiesgo;
    private Map<String, Long> distribucionPorSede;
    private Map<String, Long> distribucionPorDepartamento;
    private BigDecimal promedioImc;
    private BigDecimal medianaImc;
    private BigDecimal desviacionImc;
    private BigDecimal promedioGlucosa;
    private BigDecimal promedioColesterol;
    private BigDecimal promedioTrigliceridos;
    private BigDecimal promedioGrasaVisceral;
    private double porcentajeImcAlterado;
    private double porcentajeGlucosaAlterada;
    private double porcentajePresionAlterada;
    private double porcentajeGrasaVisceralAlterada;
    private long totalAlertasActivas;
    private long totalAlertasCriticas;
    private long totalAlertasAtendidas;
    private Map<String, Double> radarIndicadoresPromedio;
}
