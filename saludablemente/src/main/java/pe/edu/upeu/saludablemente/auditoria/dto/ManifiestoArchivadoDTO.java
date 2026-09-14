package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifiestoArchivadoDTO {
    private UUID idManifiesto;
    private LocalDateTime rangoFechaInicio;
    private LocalDateTime rangoFechaFin;
    private Integer totalRegistrosExportados;
    private String rutaColdStorage;
    private String hashManifiesto;
    private String firmaRsa;
    private LocalDateTime fechaExportacion;
    private String responsablePurga;
    private String estado;
}
