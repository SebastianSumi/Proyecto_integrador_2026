package pe.edu.upeu.saludablemente.exportacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea;
import pe.edu.upeu.saludablemente.exportacion.enums.FaseExportacion;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;
import pe.edu.upeu.saludablemente.exportacion.enums.ModoPrivacidad;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TAREA_EXPORTACION", schema = "SALUD_EXPORTACION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaExportacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TAREA", columnDefinition = "RAW(16)")
    private UUID idTarea;

    @Column(name = "ID_USUARIO_SOLICITANTE", nullable = false)
    private Long idUsuarioSolicitante;

    @Column(name = "TITULO_LOTE", nullable = false, length = 150)
    private String tituloLote;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_TAREA", nullable = false, length = 20)
    @Builder.Default
    private EstadoTarea estadoTarea = EstadoTarea.EN_COLA;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORMATO_SALIDA", nullable = false, length = 30)
    private FormatoSalida formatoSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODO_PRIVACIDAD", nullable = false, length = 30)
    private ModoPrivacidad modoPrivacidad;

    @Lob
    @Column(name = "PARAMETROS_FILTRO", nullable = false, columnDefinition = "CLOB")
    private String parametrosFiltro;

    @Column(name = "PORCENTAJE_AVANCE", nullable = false)
    @Builder.Default
    private Integer porcentajeAvance = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "FASE_ACTUAL", length = 30)
    private FaseExportacion faseActual;

    @Column(name = "TOTAL_REGISTROS_ESTIMADOS")
    private Long totalRegistrosEstimados;

    @Column(name = "TOTAL_REGISTROS_PROCESADOS")
    private Long totalRegistrosProcesados;

    @Column(name = "RUTA_ALMACENAMIENTO", length = 300)
    private String rutaAlmacenamiento;

    @Column(name = "TAMANO_BYTES")
    private Long tamanoBytes;

    @Column(name = "HASH_SHA256", length = 64)
    private String hashSha256;

    @Column(name = "ESTA_CIFRADO", nullable = false, length = 1)
    @Builder.Default
    private String estaCifrado = "N";

    @Column(name = "FECHA_SOLICITUD", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "FECHA_INICIO_PROCESO")
    private LocalDateTime fechaInicioProceso;

    @Column(name = "FECHA_FINALIZACION")
    private LocalDateTime fechaFinalizacion;

    @Lob
    @Column(name = "MENSAJE_ERROR", columnDefinition = "CLOB")
    private String mensajeError;

    public boolean isCifrado() {
        return "S".equalsIgnoreCase(this.estaCifrado);
    }

    public boolean estaCompletado() {
        return this.estadoTarea == EstadoTarea.COMPLETADO;
    }

    public boolean estaEnEjecucion() {
        return this.estadoTarea == EstadoTarea.PROCESANDO;
    }
}
