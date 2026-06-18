package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import restaurante_pos.model.Usuario;
import restaurante_pos.service.UsuarioService;
import restaurante_pos.service.SeguridadService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final SeguridadService seguridadService;

    // 🚀 ESCUDO ANTIDUPLICADOS: Guarda en memoria viva qué usuarios tienen sesión abierta y en qué ID de sesión HTTP
    private static final Map<String, String> sesionesActivas = new ConcurrentHashMap<>();

    public AuthController(UsuarioService usuarioService, SeguridadService seguridadService) {
        this.usuarioService = usuarioService;
        this.seguridadService = seguridadService;
    }

    @GetMapping("/login-page")
    public String loginPage(HttpSession session) {
        return "index"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("pin") String pin, Model model, HttpSession session) {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        for (Usuario u : usuarios) {
            if (seguridadService.verificarContrasena(pin, u.getLoginPin())) {
                
                String nombreUsuario = u.getNombre();
                String sessionIdActual = session.getId();

                // 🔒 CONTROL ESTRICTO: Verifica si el empleado ya está trabajando en otro dispositivo
                if (sesionesActivas.containsKey(nombreUsuario) && !sesionesActivas.get(nombreUsuario).equals(sessionIdActual)) {
                    System.err.println("🚨 BLOQUEO DE ACCESO CONCURRENTE: " + nombreUsuario + " intentó duplicar sesión.");
                    model.addAttribute("error", "Este usuario ya se encuentra activo en otro dispositivo o tableta.");
                    return "index";
                }

                // Si está libre, lo registramos en el mapa de control y creamos la sesión
                sesionesActivas.put(nombreUsuario, sessionIdActual);
                session.setAttribute("usuarioAutenticado", true);
                session.setAttribute("nombreUsuario", nombreUsuario);
                session.setAttribute("rolUsuario", u.getRol());
                
                String rol = u.getRol();
                if (rol != null) {
                    if (rol.equalsIgnoreCase("ADMINISTRADOR")) {
                        return "redirect:/panel"; 
                    } else if (rol.equalsIgnoreCase("MESERO")) {
                        return "redirect:/"; 
                    } else if (rol.equalsIgnoreCase("CAJERO")) {
                        return "redirect:/caja"; 
                    } else if (rol.equalsIgnoreCase("COCINA") || rol.equalsIgnoreCase("Personal de Cocina")) {
                        return "redirect:/parrilla"; 
                    } else if (rol.equalsIgnoreCase("JUGOS") || rol.equalsIgnoreCase("Personal de Barra")) {
                        return "redirect:/jugos";
                    }
                }
                return "redirect:/login-page";
            }
        }

        model.addAttribute("error", "El PIN ingresado es incorrecto o no coincide con ningún empleado.");
        return "index";
    }

    @GetMapping("/enrolar")
    public String mostrarEnrolamiento() {
        return "redirect:/login-page";
    }

    @GetMapping("/panel")
    public String mostrarPanelUsuario(Model model, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        model.addAttribute("rol", session.getAttribute("rolUsuario"));
        return "panel";
    }

    @GetMapping("/panel/empleados")
    public String administrarEmpleados(Model model, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || !rol.equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/"; 
        }
        model.addAttribute("usuarios", usuarioService.obtenerTodos());
        return "empleados";
    }

    @PostMapping("/panel/empleados/crear")
    public String crearEmpleadoDesdePanel(
            @RequestParam String nombre, 
            @RequestParam String pin, 
            @RequestParam String rol,
            HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        String rolActual = (String) session.getAttribute("rolUsuario");
        if (rolActual == null || !rolActual.equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/";
        }
        if (nombre != null && !nombre.trim().isEmpty() && pin != null && !pin.trim().isEmpty()) {
            usuarioService.registrarUsuario(nombre, pin, rol);
        }
        return "redirect:/panel/empleados"; 
    }

    @GetMapping("/panel/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || !rol.equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/";
        }
        try {
            usuarioService.eliminarUsuario(id);
        } catch (Exception e) {
            System.err.println("Error al intentar eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/panel/empleados";
    }

    // Endpoint para limpiar la sesión y salir de forma segura 🚪
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        if (nombreUsuario != null) {
            // 🚀 LIBERACIÓN: Removemos al usuario del mapa para que pueda iniciar sesión en otra tableta
            sesionesActivas.remove(nombreUsuario);
            System.out.println("🚪 Sesión cerrada y cupo liberado para: " + nombreUsuario);
        }
        session.invalidate(); 
        return "redirect:/login-page"; 
    }
}
