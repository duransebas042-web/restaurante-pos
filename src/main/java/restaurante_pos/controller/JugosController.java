package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.repository.PedidoItemRepository;
import java.util.ArrayList;
import java.util.List;

@Controller
public class JugosController {

    // 1. Atributo inmutable sin @Autowired
    private final PedidoItemRepository pedidoItemRepository;

    // 2. Inyección por constructor obligatoria
    public JugosController(PedidoItemRepository pedidoItemRepository) {
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @GetMapping("/jugos")
    public String panelJugos(Model model, HttpSession session) {
        // 🔒 CONTROL DE SEGURIDAD: Si intentan entrar a la barra sin iniciar sesión, los manda al login
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<PedidoItem> pedidosFiltrados = new ArrayList<>();
        List<PedidoItem> todosLosPedidos = pedidoItemRepository.findAll();
        
        // Filtramos para mostrar solo lo que está pendiente en barra y cocina, ignorando el carrito temporal y lo ya pagado
        for (PedidoItem item : todosLosPedidos) {
            if (item.getEstado() != EstadoPedido.CARRITO && item.getEstado() != EstadoPedido.PAGADO) {
                pedidosFiltrados.add(item);
            }
        }
        
        // Enviamos la lista limpia a la plantilla HTML
        model.addAttribute("pedidos", pedidosFiltrados);
        return "jugos";
    }
}
