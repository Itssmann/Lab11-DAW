package com.tecsup.services;
import java.util.List;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import com.tecsup.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class ProductoService {
    private final ProductoRepository repo;
    public ProductoService(ProductoRepository repo) { this.repo = repo; }
    public List<Producto> listar() { return repo.findAll(); }
    public Producto obtener(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id)); }
    @Transactional
    public Producto crear(ProductoDTO dto) { return repo.save(aplicar(new Producto(), dto)); }
    @Transactional
    public Producto actualizar(Long id, ProductoDTO dto) { return repo.save(aplicar(obtener(id), dto)); }
    @Transactional
    public void eliminar(Long id) { repo.delete(obtener(id)); }
    private Producto aplicar(Producto entity, ProductoDTO dto) {
        entity.setNombre(dto.nombre());
        entity.setPrecio(dto.precio());
        entity.setStock(dto.stock());
        entity.setCategoriaId(dto.categoriaId());
        return entity;
    }
}
