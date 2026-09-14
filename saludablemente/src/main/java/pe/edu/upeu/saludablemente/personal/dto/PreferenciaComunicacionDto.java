package pe.edu.upeu.saludablemente.personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaComunicacionDto {

    private Long idPreferencia;
    private String canalPreferido;
    private LocalTime horarioContactoInicio;
    private LocalTime horarioContactoFin;
    private Boolean aceptaRecordatorios;
}