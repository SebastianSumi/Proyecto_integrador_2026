package pe.edu.upeu.saludablemente.metas.meta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "METAS", schema = "SALUDABLEMENTE_OWNER")
@Getter
@Setter
@NoArgsConstructor
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long personaId;

    @Column(nullable = false, length = 40)
    private String tipoMeta;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(precision = 6, scale = 2)
    private BigDecimal valorActual;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMeta estado = EstadoMeta.EN_CURSO;
}
