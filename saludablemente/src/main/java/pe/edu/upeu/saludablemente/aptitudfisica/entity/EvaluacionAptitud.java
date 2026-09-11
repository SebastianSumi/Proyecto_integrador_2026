package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "evaluacion_aptitud")
public class EvaluacionAptitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvaluacionAptitud;

    private Long idPersona;

    private LocalDate fechaRegistro;

    private BigDecimal puntajeGlobal;

    private String diagnosticoAptitud;

    private Boolean sincronizado;

    private LocalDateTime fechaSincronizacion;

    @OneToMany(mappedBy = "evaluacionAptitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePruebaFisica> detalles = new ArrayList<>();
}