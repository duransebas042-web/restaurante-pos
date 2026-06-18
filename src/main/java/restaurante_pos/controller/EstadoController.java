package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.repository.PedidoItemRepository;
import java.util.Optional;

@Controller
public class EstadoController {

    // 1. Atributo inmutable sin @Autowired
    private final PedidoItemRepository pedidoItemRepository;

    // 2. Inyección por constructor obligatoria
    public EstadoController(PedidoItemRepository pedidoItemRepository) {
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @GetMapping("/estado/{id}/{nuevoEstado}/{origen}")
    public String cambiarEstado(
            @PathVariable Long id,
            @PathVariable String nuevoEstado,
            @PathVariable String origen) {
        
        Optional<PedidoItem> optionalPedido = pedidoItemRepository.findById(id);
        
        if (optionalPedido.isPresent()) {
            PedidoItem pedido = optionalPedido.get();
            String nombreMesa = pedido.getMesa();
            
            if ("parrilla".equals(origen)) {
                pedido.setEstadoComida(nuevoEstado); 
            } else if ("jugos".equals(origen)) {
                pedido.setEstadoBebida(nuevoEstado);
            }
            
            if ("ENTREGADO".equals(pedido.getEstadoComida()) && "ENTREGADO".equals(pedido.getEstadoBebida())) {
                pedido.setEstado(EstadoPedido.ENTREGADO);
            }
            
            pedidoItemRepository.save(pedido);
            
            if ("parrilla".equals(origen)) return "redirect:/parrilla";
            if ("jugos".equals(origen)) return "redirect:/jugos";
            return "redirect:/pedido/" + nombreMesa;
        }
        
        if ("parrilla".equals(origen)) return "redirect:/parrilla";
        if ("jugos".equals(origen)) return "redirect:/jugos";
        return "redirect:/";
    }
}
