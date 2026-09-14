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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PARTICION_BITACORA", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticionBitacoraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_PARTICION", columnDefinition = "RAW(16)")
    private UUID idParticion;

    @Column(name = "NOMBRE_PARTICION", nullable = false, length = 100)
    private String nombreParticion;

    @Column(name = "TABLA_ORIGEN", nullable = false, length = 60)
    private String tablaOrigen;

    @Column(name = "RANGO_FECHA_INICIO", nullable = false)
    private LocalDateTime rangoFechaInicio;

    @Column(name = "RANGO_FECHA_FIN", nullable = false)
    private LocalDateTime rangoFechaFin;

    @Column(name = "TOTAL_REGISTROS")
    private Long totalRegistros;

    @Column(name = "TAMANO_BYTES")
    private Long tamanoBytes;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVA";

    @Column(name = "ARCHIVADA", length = 1)
    @Builder.Default
    private String archivada = "N";

    @Column(name = "PURGADA", length = 1)
    @Builder.Default
    private String purgada = "N";

    @Column(name = "ID_MANIFIESTO", columnDefinition = "RAW(16)")
    private UUID idManifiesto;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;
}
