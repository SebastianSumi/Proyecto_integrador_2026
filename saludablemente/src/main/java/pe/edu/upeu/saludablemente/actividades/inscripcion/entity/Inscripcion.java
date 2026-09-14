package pe.edu.upeu.saludablemente.actividades.inscripcion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;

@Entity
@Table(name = "INSCRIPCIONES", schema = "SALUDABLEMENTE_OWNER")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ACTIVIDAD_ID", nullable = false)
    private Long actividadId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ACTIVIDAD_ID", insertable = false, updatable = false)
    private Actividad actividad;

    @Column(nullable = false)
    private Long personaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado = EstadoInscripcion.INSCRITA;

    @Column(nullable = false)
    private LocalDateTime inscritaEn;

    @Column
    private LocalDateTime canceladaEn;
}
