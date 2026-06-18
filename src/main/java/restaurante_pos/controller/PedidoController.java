package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.Plato;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.service.PlatoService;
import restaurante_pos.repository.PedidoItemRepository;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PedidoController {

    // 1. Atributos inmutables sin @Autowired
    private final PedidoItemRepository pedidoItemRepository;
    private final PlatoService platoService;

    // 2. Inyección por constructor explícita para resolver las 2 advertencias
    public PedidoController(PedidoItemRepository pedidoItemRepository, PlatoService platoService) {
        this.pedidoItemRepository = pedidoItemRepository;
        this.platoService = platoService;
    }

    @GetMapping("/pedido/{nombre}")
    public String pedido(@PathVariable String nombre, Model model, HttpSession session) {
        // 🔒 CONTROL DE SEGURIDAD: Si intentan forzar la URL sin PIN, se bloquean
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }
        
// Código CORREGIDO:
if (nombre.contains("Mesa Mesa")) {
    nombre = nombre.replace("Mesa Mesa", "Mesa"); //  Asignación limpia de String
}
        
        List<Plato> platos = platoService.obtenerListaPlatos();
        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(nombre);
        
        double totalCuenta = 0;
        for (PedidoItem item : pedidos) {
            for (Plato p : platos) {
                if (p.getNombre().equals(item.getPlato())) {
                    totalCuenta += p.getPrecio();
                    break;
                }
            }
        }
        
        // Pasamos los datos del mesero logueado a la interfaz visual
        model.addAttribute("nombreMesero", session.getAttribute("nombreUsuario"));
        model.addAttribute("mesa", nombre);
        model.addAttribute("platos", platos);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("total", totalCuenta); 
        return "pedido";
    }

    @GetMapping("/configurar/{mesa}/{plato}")
    public String configurarPlato(@PathVariable String mesa, @PathVariable String plato, Model model, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<Plato> listaMenu = platoService.obtenerListaPlatos();
        List<String> opciones = new ArrayList<>();
        
        for (Plato p : listaMenu) {
            if (p.getNombre().equalsIgnoreCase(plato)) {
                opciones = p.getAcompanamientosPorDefecto();
                break;
            }
        }
        model.addAttribute("mesa", mesa);
        model.addAttribute("plato", plato);
        model.addAttribute("opciones", opciones);
        return "configurar";
    }

    @PostMapping("/confirmar")
    public String confirmarPedido(
            @RequestParam String mesa,
            @RequestParam String plato,
            @RequestParam int cantidad,
            @RequestParam(value = "seleccionados", required = false) List<String> seleccionados,
            @RequestParam String bebida,
            @RequestParam String observacion,
            HttpSession session) {
        
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        // 🚀 Recuperamos la firma del empleado real que tiene la sesión activa
        String empleadoResponsable = (String) session.getAttribute("nombreUsuario");
        if (empleadoResponsable == null) {
            empleadoResponsable = "Mesero Anónimo";
        }
        
        List<String> acompanamientosFinales = (seleccionados != null) ? seleccionados : new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            PedidoItem nuevoItem = new PedidoItem(mesa, plato, acompanamientosFinales, bebida, observacion);
            nuevoItem.setEstado(EstadoPedido.CARRITO); 
            nuevoItem.setEstadoComida("PENDIENTE"); 
            nuevoItem.setEstadoBebida("PENDIENTE"); 
            
            // 🚀 ESTAMPADO DE AUDITORÍA: El registro ya no es fijo, se asocia al usuario real
            nuevoItem.setAtendidoPor(empleadoResponsable); 
            
            pedidoItemRepository.save(nuevoItem);
        }
        return "redirect:/pedido/" + mesa;
    }

    @GetMapping("/eliminar/{mesa}/{index}")
    public String eliminarPedido(@PathVariable String mesa, @PathVariable int index, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(mesa);
        if (pedidos != null && index >= 0 && index < pedidos.size()) {
            PedidoItem itemAEliminar = pedidos.get(index);
            pedidoItemRepository.delete(itemAEliminar);
        }
        return "redirect:/pedido/" + mesa;
    }
}
