package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "preferencia_comunicacion")
public class PreferenciaComunicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPreferencia;

    private String canalPreferido;

    private LocalTime horarioContactoInicio;

    private LocalTime horarioContactoFin;

    private Boolean aceptaRecordatorios;

    @OneToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;
}