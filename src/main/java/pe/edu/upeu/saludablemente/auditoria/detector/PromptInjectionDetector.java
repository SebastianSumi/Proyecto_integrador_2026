package pe.edu.upeu.saludablemente.auditoria.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptInjectionDetector implements AnomaliaDetector {

    private static final List<Pattern> PATRONES_INYECCION = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?previous\\s+instructions"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?(prior|previous)\\s+instructions"),
            Pattern.compile("(?i)olvida\\s+(todas\\s+)?las\\s+instrucciones"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)<\\|im_start\\|>"),
            Pattern.compile("(?i)<\\|im_end\\|>"),
            Pattern.compile("(?i)act\\s+as\\s+(a\\s+)?(different|new)"),
            Pattern.compile("(?i)actua\\s+como\\s+(un\\s+)?(nuevo|diferente)"),
            Pattern.compile("(?i)jailbreak"),
            Pattern.compile("(?i)DAN\\s+mode"),
            Pattern.compile("(?i)revela\\s+(tu\\s+)?(prompt|instrucciones)"),
            Pattern.compile("(?i)reveal\\s+(your\\s+)?(prompt|instructions)")
    );

    @Override
    public TipoAnomalia getTipoAnomalia() {
        return TipoAnomalia.PROMPT_INJECTION;
    }

    @Override
    public boolean aplica(ContextoDeteccion contexto) {
        return contexto.getTipoEntidad() != null
                && contexto.getTipoEntidad().equals("RECOMENDACION_IA");
    }

    @Override
    public TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto) {
        return noDetectada();
    }

    public TipoAnomaliaDeteccionDTO evaluarTexto(String usuario, String texto,
                                                  Long idPersona, String campo) {
        if (texto == null || texto.isBlank()) {
            return noDetectada();
        }

        for (Pattern patron : PATRONES_INYECCION) {
            if (patron.matcher(texto).find()) {
                return construirAlerta(usuario, texto, idPersona, campo, patron.pattern());
            }
        }

        return noDetectada();
    }

    private TipoAnomaliaDeteccionDTO construirAlerta(String usuario, String texto,
                                                     Long idPersona, String campo,
                                                     String patronDetectado) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("usuario", usuario);
        metadata.put("idPersona", idPersona);
        metadata.put("campoAfectado", campo);
        metadata.put("patronDetectado", patronDetectado);
        metadata.put("longitudTexto", texto.length());
        metadata.put("fragmento", texto.length() > 200 ? texto.substring(0, 200) + "..." : texto);

        log.error("PROMPT INJECTION detectado: usuario {} intento inyectar en campo {}",
                usuario, campo);

        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(true)
                .tipoAnomalia(TipoAnomalia.PROMPT_INJECTION)
                .severidad(SeveridadAnomalia.WARNING)
                .usuarioAfectado(usuario)
                .mensaje("Intento de inyeccion de prompt detectado en campo: " + campo)
                .metadataContexto(metadata)
                .build();
    }

    private TipoAnomaliaDeteccionDTO noDetectada() {
        return TipoAnomaliaDeteccionDTO.builder()
                .detectada(false)
                .tipoAnomalia(TipoAnomalia.PROMPT_INJECTION)
                .severidad(SeveridadAnomalia.INFO)
                .build();
    }
}
