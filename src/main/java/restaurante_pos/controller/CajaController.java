package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession; // 🚀 IMPORTACIÓN AGREGADA
import restaurante_pos.model.Mesa;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.Plato;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.service.MesaService;
import restaurante_pos.service.PlatoService;
import restaurante_pos.service.NotificacionService;
import restaurante_pos.repository.PedidoItemRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class CajaController {

    // 1. Declarar dependencias como 'final' para garantizar la inmutabilidad
    private final MesaService mesaService;
    private final PlatoService platoService;
    private final NotificacionService notificacionService;
    private final PedidoItemRepository pedidoItemRepository;

    // 2. Un único constructor público para la inyección correcta
    public CajaController(MesaService mesaService, 
                          PlatoService platoService, 
                          NotificacionService notificacionService, 
                          PedidoItemRepository pedidoItemRepository) {
        this.mesaService = mesaService;
        this.platoService = platoService;
        this.notificacionService = notificacionService;
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @GetMapping("/caja")
    public String panelCaja(Model model, HttpSession session) { // 🚀 SESIÓN AGREGADA COMO PARÁMETRO
        // 🔒 CONTROL DE SEGURIDAD: Bloquea el acceso a la caja si no ha iniciado sesión
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<Mesa> mesas = mesaService.obtenerMesas();
        Map<String, Double> totalesPorMesa = new ConcurrentHashMap<>();
        List<Plato> platos = platoService.obtenerListaPlatos();
        
        for (Mesa m : mesas) {
            double totalMesa = 0;
            List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(m.getNombre());
            
            if (pedidos.isEmpty()) {
                m.setEstado("LIBRE");
            } else {
                boolean tienePedidosEnviados = false;
                for (PedidoItem item : pedidos) {
                    if (item.getEstado() != EstadoPedido.CARRITO) {
                        tienePedidosEnviados = true;
                        break;
                    }
                }
                
                if (tienePedidosEnviados) {
                    m.setEstado("OCUPADA");
                } else {
                    m.setEstado("LIBRE");
                }
            }
            
            // 🚀 Cruce dinámico de precios reales con el catálogo
            for (PedidoItem item : pedidos) {
                if (item.getEstado() != EstadoPedido.CARRITO) {
                    for (Plato p : platos) {
                        if (p.getNombre().equals(item.getPlato())) {
                            totalMesa += p.getPrecio();
                            break;
                        }
                    }
                }
            }
            totalesPorMesa.put(m.getNombre(), totalMesa);
        }
        
        List<PedidoItem> historialVentasBD = pedidoItemRepository.findByEstado(EstadoPedido.PAGADO);
        
        model.addAttribute("mesas", mesas);
        model.addAttribute("totales", totalesPorMesa);
        model.addAttribute("historial", historialVentasBD);
        return "caja";
    }

    @GetMapping("/caja/pagar/{nombre}")
    public String cajaRegistrarPago(@PathVariable String nombre, HttpSession session) { // 🚀 SESIÓN AGREGADA AQUÍ TAMBIÉN
        // 🔒 CONTROL DE SEGURIDAD: Evita que cierren cuentas de forma externa mediante la URL
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(nombre);
        
        if (pedidos != null && !pedidos.isEmpty()) {
            String meseroResponsable = pedidos.get(0).getAtendidoPor();
            
            for (PedidoItem item : pedidos) {
                item.setEstado(EstadoPedido.PAGADO);
                pedidoItemRepository.save(item);
            }
            
            notificacionService.enviarAlerta(meseroResponsable, "¡La " + nombre + " ya canceló en caja!");
        }
        
        return "redirect:/caja";
    }
}
