package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "POLITICA_RETENCION", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliticaRetencionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_POLITICA", columnDefinition = "RAW(16)")
    private UUID idPolitica;

    @Column(name = "TIPO_DATO", nullable = false, unique = true, length = 40)
    private String tipoDato;

    @Column(name = "DIAS_RETENCION_CALIENTE", nullable = false)
    private Integer diasRetencionCaliente;

    @Column(name = "MESES_RETENCION_FRIO", nullable = false)
    private Integer mesesRetencionFrio;

    @Column(name = "ANIOS_RETENCION_LEGAL", nullable = false)
    private Integer aniosRetencionLegal;

    @Column(name = "PURGA_AUTOMATICA", nullable = false, length = 1)
    @Builder.Default
    private String purgaAutomatica = "S";

    @Column(name = "ACTIVA", nullable = false, length = 1)
    @Builder.Default
    private String activa = "S";

    @Column(name = "DESCRIPCION", length = 300)
    private String descripcion;
}
