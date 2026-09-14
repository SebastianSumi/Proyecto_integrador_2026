package pe.edu.upeu.saludablemente.auditoria.service;

import java.util.List;

public interface LedgerIntegrityVerifierService {

    ResultadoVerificacion verificarCadenaCompleta();

    ResultadoVerificacion verificarDesde(Long secuenciaInicio);

    boolean verificarRegistro(java.util.UUID idBitacora);

    List<Long> encontrarRegistrosRotos();

    class ResultadoVerificacion {
        private final boolean integra;
        private final long totalRegistros;
        private final List<Long> secuenciasRotas;
        private final String mensaje;

        public ResultadoVerificacion(boolean integra, long totalRegistros,
                                     List<Long> secuenciasRotas, String mensaje) {
            this.integra = integra;
            this.totalRegistros = totalRegistros;
            this.secuenciasRotas = secuenciasRotas;
            this.mensaje = mensaje;
        }

        public boolean isIntegra() { return integra; }
        public long getTotalRegistros() { return totalRegistros; }
        public List<Long> getSecuenciasRotas() { return secuenciasRotas; }
        public String getMensaje() { return mensaje; }
    }
}
