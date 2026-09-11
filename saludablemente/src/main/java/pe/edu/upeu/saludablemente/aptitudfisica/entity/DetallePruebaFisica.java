package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalle_prueba_fisica")
public class DetallePruebaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleAptitud;

    private BigDecimal valorObtenido;

    private BigDecimal puntajeParcial;

    @ManyToOne
    @JoinColumn(name = "id_evaluacion_aptitud")
    private EvaluacionAptitud evaluacionAptitud;

    @ManyToOne
    @JoinColumn(name = "id_prueba")
    private CatalogoPrueba catalogoPrueba;
}