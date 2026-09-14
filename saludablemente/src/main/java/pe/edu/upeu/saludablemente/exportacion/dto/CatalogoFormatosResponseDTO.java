package pe.edu.upeu.saludablemente.exportacion.dto;

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
public class CatalogoFormatosResponseDTO {

    private List<FormatoInfo> formatosDisponibles;
    private List<NivelPrivacidadInfo> nivelesPrivacidad;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormatoInfo {
        private String codigo;
        private String nombre;
        private String descripcion;
        private boolean soportaCifrado;
        private boolean soportaGraficos;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NivelPrivacidadInfo {
        private String codigo;
        private String nombre;
        private String descripcion;
    }
}
