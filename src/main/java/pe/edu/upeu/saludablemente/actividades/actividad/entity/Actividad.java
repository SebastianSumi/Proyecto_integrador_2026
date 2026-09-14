package pe.edu.upeu.saludablemente.actividades.actividad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

@Entity
@Table(name = "ACTIVIDADES", schema = "SALUDABLEMENTE_OWNER")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 100)
    private String lugar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoActividad estado = EstadoActividad.PROGRAMADA;

    @Column(nullable = false)
    private Long creadorId;

    @OneToMany(mappedBy = "actividad", fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();
}
