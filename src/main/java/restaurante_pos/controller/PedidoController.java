package restaurante_pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    // 1. Añade la herramienta de mensajería como un atributo final privado
private final PedidoItemRepository pedidoItemRepository;
private final PlatoService platoService;
private final SimpMessagingTemplate messagingTemplate;
// 1. Inyecta el servicio en tus atributos privados superiores
private final UsuarioService usuarioService; // <-- Añade esta línea

// 2. Recuerda actualizar tu constructor explícito para recibirlo:
public PedidoController(PedidoItemRepository pedidoItemRepository, 
                        PlatoService platoService, 
                        SimpMessagingTemplate messagingTemplate,
                        UsuarioService usuarioService) { // <-- Añade este parámetro
    this.pedidoItemRepository = pedidoItemRepository;
    this.platoService = platoService;
    this.messagingTemplate = messagingTemplate;
    this.usuarioService = usuarioService; // <-- Añade esta línea
}

/**
 * 🔒 RUTA: Renderiza la pantalla de Login
 */
@GetMapping("/login-page")
public String mostrarLogin() {
    return "login-page";
}

/**
 * 🔒 ACCIÓN: Procesa el formulario de ingreso
 */
@PostMapping("/login/procesar")
public String procesarAutenticacion(
        @RequestParam String username,
        @RequestParam String password,
        HttpSession session,
        Model model) {

    UsuarioService.EmpleadoInfo empleado = usuarioService.autenticar(username, password);

    if (empleado != null) {
        // 🚀 ESTAMPADO DE SEGURIDAD: Guardamos las credenciales reales del empleado en la sesión
        session.setAttribute("usuarioAutenticado", username);
        session.setAttribute("nombreUsuario", empleado.getNombreReal());
        session.setAttribute("rolUsuario", empleado.getRol());
        
        return "redirect:/"; // Redirige de inmediato al mapa de mesas principal
    }

    // Si las credenciales fallan, recargamos la pantalla con un mensaje de error explícito
    model.addAttribute("error", "Usuario o contraseña incorrectos. Intente de nuevo.");
    return "login-page";
}

/**
 * 🔒 RUTA: Cierre de turno / Cerrar Sesión
 */
@GetMapping("/logout")
public String cerrarSesion(HttpSession session) {
    session.invalidate(); // Destruye por completo los permisos en memoria
    return "redirect:/login-page";
}
    

// 2. Modifica tu constructor explícito para recibir la inyección automática
public PedidoController(PedidoItemRepository pedidoItemRepository, 
                        PlatoService platoService, 
                        SimpMessagingTemplate messagingTemplate) {
    this.pedidoItemRepository = pedidoItemRepository;
    this.platoService = platoService;
    this.messagingTemplate = messagingTemplate;
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

    /**
     * 🚀 VISTA DEL MONITOR DE COCINA
     * Carga todas las comandas en preparación que ya salieron del carrito temporal
     */
    @GetMapping("/cocina")
    @Transactional(readOnly = true)
    public String verMonitorCocina(Model model, HttpSession session) {
        if (session.getAttribute("usuarioAutenticado") == null) {
            return "redirect:/login-page";
        }

        List<PedidoItem> todasLasComandas = pedidoItemRepository.findAll();
        
        // Filtramos para ignorar elementos en el carrito o ya pagados/archivados
        List<PedidoItem> comandasCocina = todasLasComandas.stream()
                .filter(item -> item.getEstado() != EstadoPedido.CARRITO && item.getEstado() != EstadoPedido.PAGADO)
                .toList();

        model.addAttribute("comandas", comandasCocina);
        return "cocina";
    }

    /**
     * 🚀 ACCIÓN: LOGÍSTICA DE PRODUCCIÓN
     * Transiciona los estados de preparación en cocina y alerta automáticamente al mesero al finalizar
     */
@PostMapping("/cocina/avanzar/{id}")
@Transactional
public String avanzarEstadoPlato(@PathVariable Long id) {
    PedidoItem item = pedidoItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Comanda no encontrada con ID: " + id));

    if (item.getEstado() == EstadoPedido.PENDIENTE) {
        item.setEstado(EstadoPedido.PREPARANDO);
        
        // 🚀 WEBSOCKET: Ordena silenciosamente a la pantalla del mesero refrescar el estado a "PREPARANDO"
        messagingTemplate.convertAndSend("/topic/mesa/" + item.getMesa(), "REFRESCAR_PANTALLA");
        
    } else if (item.getEstado() == EstadoPedido.PREPARANDO) {
        item.setEstado(EstadoPedido.LISTO);
        
        // 🚀 WEBSOCKET: Envía la orden de refrescar y la notificación flotante al mismo tiempo
        messagingTemplate.convertAndSend("/topic/mesa/" + item.getMesa(), "REFRESCAR_PANTALLA");
        messagingTemplate.convertAndSend("/topic/mesa/" + item.getMesa(), "ALERTA:¡El plato '" + item.getPlato() + "' está LISTO en barra!");
    }

    pedidoItemRepository.save(item);
    return "redirect:/cocina";
}

/**
 * 🚀 VISTA DE FACTURACIÓN (PRE-CUENTA)
 * Permite al cajero ver el desglose final antes de recibir el dinero.
 */
@GetMapping("/caja/mesa/{nombre}")
@Transactional(readOnly = true)
public String verDetallePagoMesa(@PathVariable String nombre, Model model, HttpSession session) {
    if (session.getAttribute("usuarioAutenticado") == null) {
        return "redirect:/login-page";
    }

    if (nombre.contains("Mesa Mesa")) {
        nombre = nombre.replace("Mesa Mesa", "Mesa");
    }

    // Buscamos los ítems activos que no han sido pagados aún
    List<PedidoItem> pedidosActivos = pedidoItemRepository.findByMesa(nombre).stream()
            .filter(item -> item.getEstado() != EstadoPedido.CARRITO && item.getEstado() != EstadoPedido.PAGADO)
            .toList();

    BigDecimal totalCuenta = pedidosActivos.stream()
            .map(PedidoItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("mesa", nombre);
    model.addAttribute("pedidos", pedidosActivos);
    model.addAttribute("total", totalCuenta);

    return "caja-detalle";
}

/**
 * 🚀 ACCIÓN DE PAGO EFECTIVO / FACTURACIÓN
 * Cierra la cuenta, cambia estados a PAGADO y alerta al mesero.
 */
@PostMapping("/caja/pagar/{nombre}")
@Transactional
public String procesarPagoMesa(@PathVariable String nombre, HttpSession session) {
    if (session.getAttribute("usuarioAutenticado") == null) {
        return "redirect:/login-page";
    }

    if (nombre.contains("Mesa Mesa")) {
        nombre = nombre.replace("Mesa Mesa", "Mesa");
    }

    List<PedidoItem> pedidosMesa = pedidoItemRepository.findByMesa(nombre).stream()
            .filter(item -> item.getEstado() != EstadoPedido.CARRITO && item.getEstado() != EstadoPedido.PAGADO)
            .toList();

    if (pedidosMesa.isEmpty()) {
        return "redirect:/";
    }

    for (PedidoItem item : pedidosMesa) {
        item.setEstado(EstadoPedido.PAGADO);
        pedidoItemRepository.save(item);
    }

    // 🚀 WEBSOCKET: Emite la alerta de pago cerrado directo a la pantalla del mesero al instante
    messagingTemplate.convertAndSend("/topic/mesa/" + nombre, "ALERTA:¡La cuenta ha sido PAGADA en caja! Puedes liberar la mesa.");

    return "redirect:/";
}


/**
 * 🚀 DESPACHO MASIVO A PRODUCCIÓN
 * Toma todos los platos en estado 'CARRITO' de la mesa y los envía a la cocina.
 */
@GetMapping("/enviar-cocina/{mesa}")
@Transactional
public String enviarPedidoACocina(@PathVariable String mesa, HttpSession session) {
    // 1. Control de seguridad básico de sesión
    if (session.getAttribute("usuarioAutenticado") == null) {
        return "redirect:/login-page";
    }

    // Limpieza de redundancia de texto en la variable mesa si aplica
    if (mesa.contains("Mesa Mesa")) {
        mesa = mesa.replace("Mesa Mesa", "Mesa");
    }

    // 2. Traer todos los ítems actuales cargados a esa mesa en la base de datos
    List<PedidoItem> pedidosMesa = pedidoItemRepository.findByMesa(mesa);

    // 3. Filtrar y procesar únicamente los que están esperando en el CARRITO temporal
    List<PedidoItem> itemsParaCocina = pedidosMesa.stream()
            .filter(item -> item.getEstado() == EstadoPedido.CARRITO)
            .toList();

    // Si el mesero oprime el botón por accidente y el carrito está vacío, regresamos sin hacer nada
    if (itemsParaCocina.isEmpty()) {
        return "redirect:/pedido/" + mesa;
    }

    // 4. Cambiar el estado de la comanda comercial a PENDIENTE para que la cocina los lea
    for (PedidoItem item : itemsParaCocina) {
        item.setEstado(EstadoPedido.PENDIENTE);
        pedidoItemRepository.save(item); // Sincroniza el cambio en la base de datos
    }

    // 5. Redireccionar al mesero a la misma pantalla de la mesa para que vea los estados en cola
    return "redirect:/pedido/" + mesa;
}



