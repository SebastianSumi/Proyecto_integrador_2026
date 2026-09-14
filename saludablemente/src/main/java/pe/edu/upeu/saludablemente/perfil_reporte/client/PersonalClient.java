package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ColaboradorFiliacionDTO;

@Slf4j
@Component
public class PersonalClient {

    public ColaboradorFiliacionDTO obtenerFiliacion(Long idPersona) {
        log.debug("Obteniendo filiacion para persona {}", idPersona);
        return ColaboradorFiliacionDTO.builder()
                .idPersona(idPersona)
                .codigoColaborador("EMP-" + idPersona)
                .nombreCompleto("Colaborador " + idPersona)
                .areaTrabajo("Operaciones")
                .sede("Central")
                .edad(45)
                .sexo("M")
                .build();
    }
}
