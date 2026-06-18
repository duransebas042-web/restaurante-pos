package restaurante_pos.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificacionService {
    
    // Contenedor seguro para hilos que aloja las alertas volátiles de la caja 🚀
    private final Map<String, List<String>> notificacionesMeseros = new ConcurrentHashMap<>();

    public void enviarAlerta(String mesero, String mensaje) {
        this.notificacionesMeseros.computeIfAbsent(mesero, k -> new ArrayList<>()).add(mensaje);
    }

    public List<String> consumirAlertas(String mesero) {
        List<String> alertas = this.notificacionesMeseros.getOrDefault(mesero, new ArrayList<>());
        this.notificacionesMeseros.remove(mesero);
        return alertas;
    }
}
