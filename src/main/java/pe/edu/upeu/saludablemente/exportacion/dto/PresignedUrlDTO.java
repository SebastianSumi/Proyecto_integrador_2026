package pe.edu.upeu.saludablemente.exportacion.dto;

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
public class PresignedUrlDTO {

    private String url;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaExpiracion;
    private String token;
}
