package com.tecsup.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;
    @Column(nullable = false) private LocalDate fecha;
    @Column(nullable = false) private Long idCliente;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();
    public Pedido() { }
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void reemplazarDetalles(List<DetallePedido> nuevos) {
        detalles.forEach(d -> d.setPedido(null));
        detalles.clear();
        nuevos.forEach(d -> { d.setPedido(this); detalles.add(d); });
    }
}
