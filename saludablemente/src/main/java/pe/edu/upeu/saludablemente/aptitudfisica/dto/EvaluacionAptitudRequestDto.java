package pe.edu.upeu.saludablemente.aptitudfisica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EvaluacionAptitudRequestDto {

    @NotNull
    private Long idPersona;

    @NotNull
    @PastOrPresent
    private LocalDate fechaRegistro;

    @Valid
    @NotEmpty
    private List<DetallePruebaFisicaDto> detalles;

    private Boolean sincronizado;
    private LocalDateTime fechaSincronizacion;
}