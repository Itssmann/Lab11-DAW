package com.tecsup.controller;
import java.util.List;
import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.tecsup.dto.PedidoDTO;
import com.tecsup.model.Pedido;
import com.tecsup.services.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service) { this.service = service; }
    @GetMapping public List<Pedido> listar() { return service.listar(); }
    @GetMapping("/{id}") public Pedido obtener(@PathVariable @Positive Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoDTO dto) {
        Pedido entity = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/pedidos/" + entity.getIdPedido())).body(entity);
    }
    @PutMapping("/{id}") public Pedido actualizar(@PathVariable @Positive Long id, @Valid @RequestBody PedidoDTO dto) { return service.actualizar(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
