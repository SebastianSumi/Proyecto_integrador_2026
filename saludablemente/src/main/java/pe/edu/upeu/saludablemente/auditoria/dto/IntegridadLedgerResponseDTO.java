package pe.edu.upeu.saludablemente.auditoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegridadLedgerResponseDTO {
    private boolean integra;
    private long totalRegistrosVerificados;
    private List<Long> secuenciasRotas;
    private String mensaje;
    private UUID idBitacoraVerificado;
    private Boolean registroIndividualIntegro;
    private LocalDateTime fechaVerificacion;
}
