package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import restaurante_pos.model.PedidoItem;
import restaurante_pos.model.Plato;
import restaurante_pos.model.EstadoPedido;
import restaurante_pos.service.PlatoService;
import restaurante_pos.repository.PedidoItemRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PedidoController {

    private final PedidoItemRepository pedidoItemRepository;
    private final PlatoService platoService;

    // Inyección limpia por constructor
    public PedidoController(PedidoItemRepository pedidoItemRepository, PlatoService platoService) {
        this.pedidoItemRepository = pedidoItemRepository;
        this.platoService = platoService;
    }

    @GetMapping("/pedido/{nombre}")
    @Transactional(readOnly = true) // Optimiza la consulta en base de datos
    public String pedido(@PathVariable String nombre, Model model, HttpSession session) {
        
        // CONTROL DE SEGURIDAD
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        // Limpieza del nombre de la mesa
        if (nombre.contains("Mesa Mesa")) {
            nombre = nombre.replace("Mesa Mesa", "Mesa");
        }

        List<Plato> platos = platoService.obtenerListaPlatos();
        List<PedidoItem> pedidos = pedidoItemRepository.findByMesa(nombre);

        // OPTIMIZACIÓN DE RENDIMIENTO: Calculamos el total usando flujos y multiplicando por la cantidad
        // Evitamos los bucles anidados usando operaciones directas si tu PedidoItem guarda su precio unitario
        double totalCuenta = pedidos.stream()
                .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
                .sum();

        // Envío de datos a la vista Thymeleaf
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
        
        // Buscar acompañamientos de forma directa
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
    @Transactional // Asegura que la inserción sea atómica y segura
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

        String empleadoResponsable = (String) session.getAttribute("nombreUsuario");
        if (empleadoResponsable == null) {
            empleadoResponsable = "Mesero Anónimo";
        }

        List<String> acompanamientosFinales = (seleccionados != null) ? seleccionados : new ArrayList<>();

        // REFACTORIZACIÓN PROFESIONAL: Buscamos el precio real del plato para congelarlo en el item
        List<Plato> listaMenu = platoService.obtenerListaPlatos();
        double precioActual = 0;
        for (Plato p : listaMenu) {
            if (p.getNombre().equalsIgnoreCase(plato)) {
                precioActual = p.getPrecio();
                break;
            }
        }

        // En lugar de hacer un bucle para guardar N veces el mismo objeto, 
        // guardamos un único registro con la cantidad seleccionada.
        PedidoItem nuevoItem = new PedidoItem(mesa, plato, acompanamientosFinales, bebida, observacion);
        nuevoItem.setCantidad(cantidad); // <-- Agrega este atributo en tu clase PedidoItem
        nuevoItem.setPrecioUnitario(precioActual); // <-- Agrega este atributo para blindar los precios
        nuevoItem.setEstado(EstadoPedido.CARRITO);
        nuevoItem.setEstadoComida("PENDIENTE");
        nuevoItem.setEstadoBebida("PENDIENTE");
        nuevoItem.setAtendidoPor(empleadoResponsable);

        pedidoItemRepository.save(nuevoItem);

        return "redirect:/pedido/" + mesa;
    }

    @GetMapping("/eliminar/{mesa}/{index}")
    @Transactional // Vital para operaciones de borrado
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

