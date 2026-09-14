package pe.edu.upeu.saludablemente.auditoria.service;

import java.util.Map;

public interface DataMaskingService {

    Map<String, Object> enmascararMapa(Map<String, Object> datos);

    String enmascararTexto(String texto);

    Object enmascararValor(String clave, Object valor);
}
