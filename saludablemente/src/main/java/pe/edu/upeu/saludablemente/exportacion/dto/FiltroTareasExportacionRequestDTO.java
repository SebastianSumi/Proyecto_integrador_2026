package pe.edu.upeu.saludablemente.exportacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroTareasExportacionRequestDTO {

    private EstadoTarea estado;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;

    @Builder.Default
    private Integer pagina = 0;

    @Builder.Default
    private Integer limite = 10;
}
