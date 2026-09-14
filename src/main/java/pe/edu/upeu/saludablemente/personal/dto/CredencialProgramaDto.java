package pe.edu.upeu.saludablemente.personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredencialProgramaDto {

    private Long idCredencial;
    private String codigoQrHash;
    private String tipoCredencial;
    private LocalDateTime fechaEmision;
    private String estado;
}