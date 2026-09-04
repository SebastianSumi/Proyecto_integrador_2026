package pe.edu.upeu.saludablemente.alertas_clinicas.scoring_inteligente.exception;

public class HistoricoNoDisponibleException extends ScoringInteligenteException {

    public HistoricoNoDisponibleException(Long idPersona) {
        super(
                String.format("Histórico no disponible para persona %d", idPersona),
                "HISTORICO_NO_DISPONIBLE",
                "No se pudo obtener el historial clínico del paciente"
        );
    }
}