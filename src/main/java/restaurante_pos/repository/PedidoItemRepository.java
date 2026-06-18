package restaurante_pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.EstadoPedido;
import java.util.List;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
    
    // Método personalizado automático: Busca los pedidos de una mesa específica
    List<PedidoItem> findByMesa(String mesa);
    
    // Método personalizado automático: Busca los registros históricos del restaurante por su estado (ej. PAGADO)
    List<PedidoItem> findByEstado(EstadoPedido estado);
}
