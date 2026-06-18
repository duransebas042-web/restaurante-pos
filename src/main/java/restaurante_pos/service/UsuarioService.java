package restaurante_pos.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // 🚀 Importación para ejecutar al arrancar
import restaurante_pos.model.Usuario;
import restaurante_pos.repository.UsuarioRepository;
import java.util.List;

@Service
public class UsuarioService {

    // 1. Atributos inmutables sin @Autowired
    private final UsuarioRepository usuarioRepository;
    private final SeguridadService seguridadService;

    // 2. Inyección por constructor explícita para resolver las 2 advertencias
    public UsuarioService(UsuarioRepository usuarioRepository, SeguridadService seguridadService) {
        this.usuarioRepository = usuarioRepository;
        this.seguridadService = seguridadService;
    }

    /**
     * 🚀 MÉTODO DE RESCATE: Se ejecuta automáticamente al encender el servidor.
     * Si la tabla de usuarios en disco está completamente vacía, creará tu acceso 
     * maestro encriptado al instante para que no te quedes por fuera.
     */
    @PostConstruct
    public void inicializarAdministrador() {
        if (usuarioRepository.count() == 0) {
            // Registramos tu usuario maestro con el PIN de prueba seguro "1234"
            registrarUsuario("Johan Sebastián Duran Cruz", "1234", "ADMINISTRADOR");
            System.out.println("🔒 RESCATE POS: Se creó el Administrador Maestro con PIN encriptado con éxito.");
        }
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
