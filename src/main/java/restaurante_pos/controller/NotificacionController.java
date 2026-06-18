package restaurante_pos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import restaurante_pos.service.NotificacionService;
import java.util.List;

@RestController 
public class NotificacionController {

    // 1. Atributo inmutable sin @Autowired
    private final NotificacionService notificacionService;

    // 2. Inyección por constructor obligatoria
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/mesero/alertas/{mesa}")
    public List<String> obtenerAlertasMesero(@PathVariable String mesa) {
        String clave = mesa + "-servicio";
        return notificacionService.consumirAlertas(clave);
    }
}
