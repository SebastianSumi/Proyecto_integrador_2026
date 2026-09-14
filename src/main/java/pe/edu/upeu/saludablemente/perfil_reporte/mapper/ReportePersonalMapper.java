package pe.edu.upeu.saludablemente.perfil_reporte.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteGeneradoResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteHistorialResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReportePersonalEntity;

@Mapper(componentModel = "spring")
public interface ReportePersonalMapper {

    @Mapping(target = "hashIntegridad", source = "hashIntegridadSha256")
    @Mapping(target = "urlDescarga",
            expression = "java(\"/api/v1/perfil/reportes/\" + entity.getIdReporte() + \"/descargar\")")
    ReporteGeneradoResponseDTO toGeneradoResponseDTO(ReportePersonalEntity entity);

    @Mapping(target = "tamanoFormateado",
            expression = "java(formatearTamano(entity.getTamanoBytes()))")
    @Mapping(target = "disponibleParaDescarga",
            expression = "java(entity.getRutaAlmacenamiento() != null)")
    ReporteHistorialResponseDTO toHistorialDTO(ReportePersonalEntity entity);

    default String formatearTamano(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
