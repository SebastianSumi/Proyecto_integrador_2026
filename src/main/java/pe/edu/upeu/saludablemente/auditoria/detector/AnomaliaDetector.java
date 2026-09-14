package pe.edu.upeu.saludablemente.auditoria.detector;

import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;

import java.time.LocalDateTime;

public interface AnomaliaDetector {

    TipoAnomaliaDeteccionDTO evaluar(ContextoDeteccion contexto);

    TipoAnomalia getTipoAnomalia();

    default boolean aplica(ContextoDeteccion contexto) {
        return true;
    }

    class ContextoDeteccion {
        private String usuario;
        private String direccionIp;
        private String userAgent;
        private String uri;
        private String metodoHttp;
        private String tipoEntidad;
        private String idEntidad;
        private String tipoOperacion;
        private Long idPersona;
        private LocalDateTime timestamp;

        public ContextoDeteccion() {
        }

        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }

        public String getDireccionIp() { return direccionIp; }
        public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }

        public String getMetodoHttp() { return metodoHttp; }
        public void setMetodoHttp(String metodoHttp) { this.metodoHttp = metodoHttp; }

        public String getTipoEntidad() { return tipoEntidad; }
        public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }

        public String getIdEntidad() { return idEntidad; }
        public void setIdEntidad(String idEntidad) { this.idEntidad = idEntidad; }

        public String getTipoOperacion() { return tipoOperacion; }
        public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }

        public Long getIdPersona() { return idPersona; }
        public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final ContextoDeteccion ctx = new ContextoDeteccion();

            public Builder usuario(String v) { ctx.usuario = v; return this; }
            public Builder direccionIp(String v) { ctx.direccionIp = v; return this; }
            public Builder userAgent(String v) { ctx.userAgent = v; return this; }
            public Builder uri(String v) { ctx.uri = v; return this; }
            public Builder metodoHttp(String v) { ctx.metodoHttp = v; return this; }
            public Builder tipoEntidad(String v) { ctx.tipoEntidad = v; return this; }
            public Builder idEntidad(String v) { ctx.idEntidad = v; return this; }
            public Builder tipoOperacion(String v) { ctx.tipoOperacion = v; return this; }
            public Builder idPersona(Long v) { ctx.idPersona = v; return this; }
            public Builder timestamp(LocalDateTime v) { ctx.timestamp = v; return this; }

            public ContextoDeteccion build() { return ctx; }
        }
    }
}
