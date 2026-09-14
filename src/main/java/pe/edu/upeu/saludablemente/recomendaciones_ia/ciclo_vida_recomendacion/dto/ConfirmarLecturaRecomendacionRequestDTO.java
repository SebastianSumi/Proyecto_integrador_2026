package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarLecturaRecomendacionRequestDTO {

    @NotNull
    private UUID idRecomendacion;
    @NotNull
    private Boolean conformidadLeida;
}