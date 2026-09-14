package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaUsuarioDTO {
    private Long idUsuario;
    private int exportacionesUltimaHora;
    private int limiteExportacionesPorHora;
    private long registrosUltimas24Horas;
    private long limiteRegistrosDiarios;
    private boolean cuotaDisponible;
    private String motivoBloqueo;
}
