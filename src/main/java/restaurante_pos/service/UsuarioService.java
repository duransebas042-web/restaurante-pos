package restaurante_pos.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import restaurante_pos.model.Usuario;
import restaurante_pos.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SeguridadService seguridadService;

    // Inyección por constructor explícita
    public UsuarioService(UsuarioRepository usuarioRepository, SeguridadService seguridadService) {
        this.usuarioRepository = usuarioRepository;
        this.seguridadService = seguridadService;
    }

    /**
     * 🚀 MÉTODO DE RESCATE: Se ejecuta automáticamente al encender el servidor.
     * Si la tabla de usuarios en disco está completamente vacía, creará tu acceso
     * maestro al instante para que no te quedes por fuera.
     */
    @PostConstruct
    public void inicializarAdministrador() {
        if (usuarioRepository.count() == 0) {
            // Registramos tu usuario maestro con el PIN de prueba seguro "1234"
            registrarUsuario("Johan Sebastián Duran Cruz", "1234", "ADMINISTRADOR");
            System.out.println("🔐 RESCATE POS: Se creó el Administrador Maestro con PIN encriptado con éxito.");
        }
    }

    /**
     * 🚀 MÉTODO OPERATIVO DE AUTENTICACIÓN
     * Busca al empleado por su nombre exacto y verifica si su PIN coincide usando SeguridadService.
     */
    public Usuario autenticar(String nombre, String pinPlano) {
        // Buscamos en la base de datos si existe el empleado por su nombre
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombre(nombre);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Le pedimos a tu SeguridadService que verifique si el PIN plano coincide con el encriptado
            if (seguridadService.verificarContrasena(pinPlano, usuario.getPin())) { // <-- Ajusta .getPin() según tu modelo Usuario
                return usuario; // Credenciales válidas, retorna el objeto completo
            }
        }
        return null; // Autenticación fallida
    }

    public boolean requiereEnrolamiento() {
        return usuarioRepository.count() == 0;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public void registrarUsuario(String nombre, String pinPlano, String rol) {
        String pinEncriptado = seguridadService.encriptarContrasena(pinPlano);
        Usuario nuevoUsuario = new Usuario(nombre, pinEncriptado, rol);
        usuarioRepository.save(nuevoUsuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
