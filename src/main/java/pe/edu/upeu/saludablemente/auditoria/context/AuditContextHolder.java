package pe.edu.upeu.saludablemente.auditoria.context;

import pe.edu.upeu.saludablemente.auditoria.dto.AuditContextDTO;

public final class AuditContextHolder {

    private static final ThreadLocal<AuditContextDTO> CONTEXT = new ThreadLocal<>();

    private AuditContextHolder() {
    }

    public static void set(AuditContextDTO context) {
        CONTEXT.set(context);
    }

    public static AuditContextDTO get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }
}
