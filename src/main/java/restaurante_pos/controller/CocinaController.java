package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession; // 🚀 IMPORTACIÓN AGREGADA
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.repository.PedidoItemRepository;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CocinaController {

    // 1. Atributo inmutable sin @Autowired
    private final PedidoItemRepository pedidoItemRepository;

    // 2. Inyección por constructor obligatoria
    public CocinaController(PedidoItemRepository pedidoItemRepository) {
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @GetMapping("/enviar-cocina/{mesa}")
    public String enviarACocina(@PathVariable String mesa) {
        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(mesa);
        if (pedidos != null) {
            for (PedidoItem pedido : pedidos) {
                if (pedido.getEstado() == EstadoPedido.CARRITO) {
                    pedido.setEstado(EstadoPedido.PENDIENTE);
                    pedidoItemRepository.save(pedido);
                }
            }
        }
        return "redirect:/pedido/" + mesa;
    }

    @GetMapping("/parrilla")
    public String panelParrilla(Model model, HttpSession session) { // 🚀 SESIÓN AGREGADA COMO PARÁMETRO
        // 🔒 CONTROL DE SEGURIDAD: Bloquea el acceso directo si no se ha validado el PIN
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<PedidoItem> pedidosFiltrados = new ArrayList<>();
        List<PedidoItem> todosLosPedidos = pedidoItemRepository.findAll();
        
        for (PedidoItem item : todosLosPedidos) {
            if (item.getEstado() != EstadoPedido.CARRITO && item.getEstado() != EstadoPedido.PAGADO) {
                pedidosFiltrados.add(item);
            }
        }
        model.addAttribute("pedidos", pedidosFiltrados);
        return "parrilla";
    }
}
