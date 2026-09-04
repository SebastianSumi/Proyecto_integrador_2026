package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CATALOGO_ACCION_CORRECTIVA", schema = "SALUD_ALERTA_CLINICA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoAccionCorrectivaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_accion_correctiva")
    @SequenceGenerator(name = "seq_accion_correctiva", sequenceName = "SEQ_ACCION_CORRECTIVA", allocationSize = 1)
    @Column(name = "ID_ACCION")
    private Long idAccion;

    @Column(name = "CODIGO_TIPIFICADO", unique = true, nullable = false, length = 40)
    private String codigoTipificado;

    @Column(name = "DESCRIPCION", nullable = false, length = 150)
    private String descripcion;

    @Column(name = "REQUIERE_SEGUIMIENTO", length = 1)
    private String requiereSeguimiento;

    public boolean isRequiereSeguimiento() {
        return "S".equalsIgnoreCase(requiereSeguimiento);
    }
}