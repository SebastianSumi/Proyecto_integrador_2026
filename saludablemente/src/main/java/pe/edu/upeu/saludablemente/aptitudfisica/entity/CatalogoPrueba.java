package pe.edu.upeu.saludablemente.aptitudfisica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "catalogo_prueba")
public class CatalogoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prueba")
    private Long id;

    @Column(name = "nombre_prueba", nullable = false, length = 100)
    private String nombrePrueba;

    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "catalogoPrueba", fetch = FetchType.LAZY)
    private List<DetallePruebaFisica> detalles = new ArrayList<>();
}