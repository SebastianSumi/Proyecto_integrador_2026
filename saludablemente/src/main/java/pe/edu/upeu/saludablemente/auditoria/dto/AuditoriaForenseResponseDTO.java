package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaForenseResponseDTO {
    private long totalRegistros;
    private int pagina;
    private int totalPaginas;
    private int limite;
    private List<EventoAuditoriaDetalleDTO> eventos;
}
