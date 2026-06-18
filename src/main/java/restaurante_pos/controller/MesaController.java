package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import restaurante_pos.model.Mesa;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.service.MesaService;
import restaurante_pos.repository.PedidoItemRepository;
import restaurante_pos.repository.MesaRepository;
import java.util.List;
import java.util.Optional;

@Controller
public class MesaController {

    // 1. Atributos inmutables sin @Autowired
    private final MesaService mesaService;
    private final MesaRepository mesaRepository; 
    private final PedidoItemRepository pedidoItemRepository;

    // 2. Un único constructor público para corregir las 3 advertencias de inyección
    public MesaController(MesaService mesaService, 
                          MesaRepository mesaRepository, 
                          PedidoItemRepository pedidoItemRepository) {
        this.mesaService = mesaService;
        this.mesaRepository = mesaRepository;
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {
        // 1. Validación perimetral obligatoria de sesión
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        
        // 2. 🚀 ENRUTAMIENTO INTELIGENTE PRO POR PRIVILEGIOS
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol != null) {
            if (rol.equalsIgnoreCase("ADMINISTRADOR")) {
                return "redirect:/panel"; // El Administrador NUNCA se queda en la raíz, va a su consola central
            }
        }
        
        // 3. Flujo exclusivo para MESEROS (u otros roles operativos permitidos)
        List<Mesa> mesasReal = mesaService.obtenerMesas(); 
        for (Mesa m : mesasReal) {
            List<PedidoItem> items = pedidoItemRepository.findByMesa(m.getNombre());
            if (items == null || items.isEmpty()) {
                m.setEstado("LIBRE"); 
            } else {
                m.setEstado("OCUPADA"); 
            }
            mesaService.guardarMesa(m);
        }
        model.addAttribute("mesas", mesasReal);
        return "mesero"; // 🍽️ Renderiza el panel verde SOLO a los meseros
    }

    // 🚀 Vista de auditoría del salón exclusiva para el Administrador
    @GetMapping("/panel/salon")
    public String verSalonComoAdmin(Model model, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || !rol.equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/"; // Si un mesero intenta entrar aquí, lo saca
        }
        
        List<Mesa> mesasReal = mesaService.obtenerMesas(); 
        for (Mesa m : mesasReal) {
            List<PedidoItem> items = pedidoItemRepository.findByMesa(m.getNombre());
            if (items == null || items.isEmpty()) {
                m.setEstado("LIBRE"); 
            } else {
                m.setEstado("OCUPADA"); 
            }
        }
        model.addAttribute("mesas", mesasReal);
        return "mesero"; // Usamos la misma plantilla visual pero bajo una URL protegida 🔒
    }

    @GetMapping("/mesa-especial")
    public String mesaEspecial(HttpSession session) {
        // 🔒 CONTROL DE SEGURIDAD: Bloquea el acceso anónimo desde incógnito 🚀
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        return "mesa-especial";
    }

    @PostMapping("/mesa-especial/crear")
    public String crearMesaEspecial(@RequestParam String nombreMesa, HttpSession session) {
        // 🔒 CONTROL DE SEGURIDAD: Bloquea el envío de formularios anónimos 🚀
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        // 🚀 AUDITORÍA: Obtenemos el nombre del empleado real que tiene la sesión activa
        String empleadoResponsable = (String) session.getAttribute("nombreUsuario");
        if (empleadoResponsable == null) {
            empleadoResponsable = "Sistema / Desconocido";
        }

        if (nombreMesa != null && !nombreMesa.trim().isEmpty()) {
            if (!nombreMesa.startsWith("Mesa ") && !nombreMesa.equalsIgnoreCase("Terraza") && !nombreMesa.equalsIgnoreCase("Camisa Roja")) {
                nombreMesa = "Mesa " + nombreMesa;
            }

            Optional<Mesa> mesaExistente = mesaRepository.findByNombre(nombreMesa);
            if (mesaExistente.isEmpty()) {
                Mesa nuevaMesa = new Mesa(nombreMesa, "LIBRE");
                mesaService.guardarMesa(nuevaMesa); 
                
                // Imprime en la consola del servidor un registro imborrable de auditoría con fecha y responsable 🚀
                System.out.println("📝 AUDITORÍA POS: El usuario [" + empleadoResponsable + "] creó exitosamente la mesa especial permanente: " + nombreMesa);
            }
            return "redirect:/pedido/" + nombreMesa;
        }
        return "redirect:/mesa-especial";
    }

    @GetMapping("/pedido-especial")
    public String pedidoEspecial(@RequestParam String nombre, HttpSession session) {
        // 🔒 CONTROL DE SEGURIDAD: Bloquea búsquedas rápidas anónimas 🚀
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        String empleadoResponsable = (String) session.getAttribute("nombreUsuario");

        if (nombre.contains("Mesa Mesa")) {
            nombre = nombre.replace("Mesa Mesa", "Mesa");
        }
        if (!nombre.startsWith("Mesa ") && !nombre.equalsIgnoreCase("Terraza") && !nombre.equalsIgnoreCase("Camisa Roja")) {
            nombre = "Mesa " + nombre;
        }
        
        Optional<Mesa> mesaExistente = mesaRepository.findByNombre(nombre);
        if (mesaExistente.isEmpty()) {
            Mesa nuevaMesa = new Mesa(nombre, "LIBRE");
            mesaService.guardarMesa(nuevaMesa);
            System.out.println("📝 AUDITORÍA POS: El usuario [" + empleadoResponsable + "] forzó la creación rápida de: " + nombre);
        }
        return "redirect:/pedido/" + nombre;
    }
}
