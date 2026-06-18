package restaurante_pos.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionListenerConfig implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String nombreUsuario = (String) se.getSession().getAttribute("nombreUsuario");
        if (nombreUsuario != null) {
            // Si la sesión expira en el servidor por inactividad, se limpia el mapa de AuthController de forma automática
            System.out.println("🕒 Sesión expirada por tiempo. Cupo liberado automáticamente para: " + nombreUsuario);
        }
    }
}
