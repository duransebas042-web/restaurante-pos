package restaurante_pos.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificacionService {

    // Contenedor seguro para hilos que aloja las alertas volátiles indexadas por MESA 🚀
    private final Map<String, List<String>> notificacionesMeseros = new ConcurrentHashMap<>();

    /**
     * Registra una nueva alerta asociada a una mesa (Ej: "Mesa 1").
     * Puede ser llamado desde el controlador de caja al recibir un pago.
     */
    public void enviarAlerta(String mesa, String mensaje) {
        this.notificacionesMeseros.computeIfAbsent(mesa, k -> new ArrayList<>()).add(mensaje);
    }

    /**
     * Consume y destruye las alertas de la mesa para el ciclo actual de la interfaz web.
     */
    public List<String> consumirAlertas(String mesa) {
        // Obtenemos y removemos la lista de la estructura en un solo paso atómico
        List<String> alertas = this.notificacionesMeseros.remove(mesa);
        
        // Si no había alertas para esa mesa, retornamos una lista vacía segura
        if (alertas == null) {
            return new ArrayList<>();
        }
        
        return alertas;
    }
}
