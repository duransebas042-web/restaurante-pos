package restaurante_pos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Activa el manejo de mensajes mediante WebSockets
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker de mensajes en memoria. 
        // El prefijo "/topic" servirá para que los meseros se "suscriban" a los cambios de sus mesas.
        config.enableSimpleBroker("/topic");
        
        // Prefijo para los mensajes que el cliente envíe hacia el servidor (si aplicara)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registramos el punto de conexión (Endpoint) donde el navegador web se conectará.
        // Permitimos conexiones desde cualquier origen para que funcione con las IPs de los celulares de los meseros.
        registry.addEndpoint("/ws-pos")
                .setAllowedOriginPatterns("*");
    }
}
