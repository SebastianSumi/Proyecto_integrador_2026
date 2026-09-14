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
public class GeoLocationDTO {
    private String direccionIp;
    private String ciudad;
    private String pais;
    private String codigoPais;
    private Double latitud;
    private Double longitud;
    private boolean ubicacionValida;
}
