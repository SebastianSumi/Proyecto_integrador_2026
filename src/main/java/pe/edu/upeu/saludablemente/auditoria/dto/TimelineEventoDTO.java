package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventoDTO {
    private UUID idBitacora;
    private Long secuencia;
    private String tipoOperacion;
    private String usuarioAutor;
    private LocalDateTime fechaRegistro;
    private String descripcionEvento;
    private List<CambioAtomicoViewDTO> cambios;
    private Map<String, Object> estadoReconstruido;
}
