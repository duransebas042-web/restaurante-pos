package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;

import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.Plato;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.service.PlatoService;
import restaurante_pos.service.NotificacionService;
import restaurante_pos.repository.PedidoItemRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PedidoController {

    private final PedidoItemRepository pedidoItemRepository;
    private final PlatoService platoService;
    private final NotificacionService notificacionService;

    // Inyección explícita por constructor de todas las dependencias requeridas
    public PedidoController(PedidoItemRepository pedidoItemRepository, 
                            PlatoService platoService, 
                            NotificacionService notificacionService) {
        this.pedidoItemRepository = pedidoItemRepository;
        this.platoService = platoService;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/pedido/{nombre}")
    @Transactional(readOnly = true)
    public String pedido(@PathVariable String nombre, Model model, HttpSession session) {
        // CONTROL DE SEGURIDAD: Bloqueo si no hay sesión iniciada
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        // Limpieza de redundancias en la cadena de texto de la mesa
        if (nombre.contains("Mesa Mesa")) {
            nombre = nombre.replace("Mesa Mesa", "Mesa");
        }

        List<Plato> platos = platoService.obtenerListaPlatos();
        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(nombre);

        // OPTIMIZACIÓN FINANCIERA: Suma matemática exacta de subtotales con BigDecimal
        BigDecimal totalCuenta = pedidos.stream()
                .map(PedidoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pasamos los datos del mesero y la mesa a la interfaz visual de Thymeleaf
        model.addAttribute("nombreMesero", session.getAttribute("nombreUsuario"));
        model.addAttribute("mesa", nombre);
        model.addAttribute("platos", platos);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("total", totalCuenta);

        return "pedido";
    }

    @GetMapping("/configurar/{mesa}/{plato}")
    @Transactional(readOnly = true)
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
    @Transactional
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

        // Recuperamos la firma del empleado real que tiene la sesión activa
        String empleadoResponsable = (String) session.getAttribute("nombreUsuario");
        if (empleadoResponsable == null) {
            empleadoResponsable = "Mesero Anónimo";
        }

        List<String> acompanamientosFinales = (seleccionados != null) ? seleccionados : new ArrayList<>();

        // Buscamos el precio comercial del plato para blindar el histórico de la venta
        List<Plato> listaMenu = platoService.obtenerListaPlatos();
        BigDecimal precioActual = BigDecimal.ZERO;
        for (Plato p : listaMenu) {
            if (p.getNombre().equalsIgnoreCase(plato)) {
                precioActual = BigDecimal.valueOf(p.getPrecio());
                break;
            }
        }

        // OPTIMIZACIÓN OPERATIVA: Guardamos un solo registro consolidado con la cantidad elegida
        PedidoItem nuevoItem = new PedidoItem(mesa, plato, acompanamientosFinales, bebida, observacion);
        nuevoItem.setCantidad(cantidad);
        nuevoItem.setPrecioUnitario(precioActual);
        nuevoItem.setEstado(EstadoPedido.CARRITO);
        nuevoItem.setEstadoComida("PENDIENTE");
        nuevoItem.setEstadoBebida("PENDIENTE");
        nuevoItem.setAtendidoPor(empleadoResponsable);

        pedidoItemRepository.save(nuevoItem);

        return "redirect:/pedido/" + mesa;
    }

    @GetMapping("/eliminar/{mesa}/{index}")
    @Transactional
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

    /**
     * 🚀 ENDPOINT DE ALERTAS ASÍNCRONAS INTEGRADO
     * Atiende las peticiones fetch de JavaScript cada 4 segundos.
     * Retorna datos puros JSON gracias a @ResponseBody sin alterar las vistas HTML.
     */
    @GetMapping("/mesero/alertas/{mesa}")
    @ResponseBody
    public List<String> verificarNovedadesDeMesa(@PathVariable String mesa) {
        return notificacionService.consumirAlertas(mesa);
    }
}
