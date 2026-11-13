package com.tecsup.services;
import java.util.List;
import java.math.BigDecimal;
import com.tecsup.dto.PedidoDTO;
import com.tecsup.model.Pedido;
import com.tecsup.model.DetallePedido;
import com.tecsup.repository.PedidoRepository;
import com.tecsup.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class PedidoService {
    private final PedidoRepository repo;
    public PedidoService(PedidoRepository repo) { this.repo = repo; }
    // Cargar detalles dentro de la transacción con open-in-view desactivado.
    public List<Pedido> listar() { List<Pedido> pedidos = repo.findAll(); pedidos.forEach(p -> p.getDetalles().size()); return pedidos; }
    public Pedido obtener(Long id) {
        Pedido pedido = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        pedido.getDetalles().size();
        return pedido;
    }
    @Transactional public Pedido crear(PedidoDTO dto) { return repo.save(aplicar(new Pedido(), dto)); }
    @Transactional public Pedido actualizar(Long id, PedidoDTO dto) { return repo.save(aplicar(obtener(id), dto)); }
    @Transactional public void eliminar(Long id) { repo.delete(obtener(id)); }
    private Pedido aplicar(Pedido pedido, PedidoDTO dto) {
        BigDecimal total = dto.detalles().stream().map(d -> d.precio().multiply(BigDecimal.valueOf(d.cantidad()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) throw new IllegalArgumentException("El total excede el importe máximo permitido");
        if (dto.total() != null && dto.total().compareTo(total) != 0) throw new IllegalArgumentException("El total debe coincidir con la suma de los detalles");
        pedido.setFecha(dto.fecha()); pedido.setIdCliente(dto.idCliente()); pedido.setTotal(total);
        pedido.reemplazarDetalles(dto.detalles().stream().map(d -> {
            DetallePedido detalle = new DetallePedido();
            detalle.setIdProducto(d.idProducto()); detalle.setCantidad(d.cantidad()); detalle.setPrecio(d.precio());
            return detalle;
        }).toList());
        return pedido;
    }
}
