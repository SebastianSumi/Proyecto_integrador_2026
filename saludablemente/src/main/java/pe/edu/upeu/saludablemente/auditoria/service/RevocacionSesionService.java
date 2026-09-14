package pe.edu.upeu.saludablemente.auditoria.service;

public interface RevocacionSesionService {

    void revocarSesion(String usuario, String motivo);

    boolean estaRevocado(String usuario);
}
