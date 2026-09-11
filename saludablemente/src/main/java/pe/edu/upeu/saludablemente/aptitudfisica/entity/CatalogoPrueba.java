package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "catalogo_prueba")
public class CatalogoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrueba;

    private String nombrePrueba;

    private String unidadMedida;

    private String descripcion;

    private Boolean activo;

    @OneToMany(mappedBy = "catalogoPrueba")
    private List<DetallePruebaFisica> detalles = new ArrayList<>();
}