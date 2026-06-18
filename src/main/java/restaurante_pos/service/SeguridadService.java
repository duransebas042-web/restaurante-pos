package restaurante_pos.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importación corregida 🚀

@Service
public class SeguridadService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Transforma un PIN plano (ej: "1234") en un hash ilegible (ej: "$2a$10$X...")
    public String encriptarContrasena(String textoPlano) {
        return encoder.encode(textoPlano);
    }

    // Compara el PIN que digita el mesero con el hash guardado en la base de datos
    public boolean verificarContrasena(String textoPlano, String hashBaseDatos) {
        return encoder.matches(textoPlano, hashBaseDatos);
    }
}
