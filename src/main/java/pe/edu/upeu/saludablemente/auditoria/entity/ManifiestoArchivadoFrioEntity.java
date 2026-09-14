package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoManifiesto;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MANIFIESTO_ARCHIVADO_FRIO", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifiestoArchivadoFrioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_MANIFIESTO", columnDefinition = "RAW(16)")
    private UUID idManifiesto;

    @Column(name = "RANGO_FECHA_INICIO", nullable = false)
    private LocalDateTime rangoFechaInicio;

    @Column(name = "RANGO_FECHA_FIN", nullable = false)
    private LocalDateTime rangoFechaFin;

    @Column(name = "TOTAL_REGISTROS_EXPORTADOS", nullable = false)
    private Integer totalRegistrosExportados;

    @Column(name = "RUTA_COLD_STORAGE", nullable = false, length = 300)
    private String rutaColdStorage;

    @Column(name = "TAMANO_ARCHIVO_BYTES", nullable = false)
    private Long tamanoArchivoBytes;

    @Column(name = "HASH_MANIFIESTO", nullable = false, length = 64)
    private String hashManifiesto;

    @Column(name = "FIRMA_RSA", length = 1000)
    private String firmaRsa;

    @Column(name = "FECHA_EXPORTACION", nullable = false)
    private LocalDateTime fechaExportacion;

    @Column(name = "RESPONSABLE_PURGA", nullable = false, length = 100)
    private String responsablePurga;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    @Builder.Default
    private EstadoManifiesto estado = EstadoManifiesto.GENERADO;

    @Column(name = "FECHA_PURGA")
    private LocalDateTime fechaPurga;
}
