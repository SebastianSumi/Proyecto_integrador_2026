package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EVALUACION_APTITUD", schema = "SLB_APTITUDFISICA")
public class EvaluacionAptitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_aptitud")
    private Long id;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "puntaje_global", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeGlobal;

    @Column(name = "diagnostico_aptitud", nullable = false, length = 50)
    private String diagnosticoAptitud;

    @Column(name = "sincronizado", nullable = false)
    private Boolean sincronizado;

    @Column(name = "fecha_sincronizacion")
    private LocalDateTime fechaSincronizacion;

    @OneToMany(mappedBy = "evaluacionAptitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePruebaFisica> detalles = new ArrayList<>();
}