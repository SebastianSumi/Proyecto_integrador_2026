package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_prueba_fisica", schema = "SLB_APTITUDFISICA")
public class DetallePruebaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_aptitud")
    private Long id;

    @Column(name = "valor_obtenido", nullable = false, precision = 6, scale = 2)
    private BigDecimal valorObtenido;

    @Column(name = "puntaje_parcial", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeParcial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_aptitud", nullable = false)
    private EvaluacionAptitud evaluacionAptitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prueba", nullable = false)
    private CatalogoPrueba catalogoPrueba;
}