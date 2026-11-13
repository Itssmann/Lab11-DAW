# Ejecutar la colección Postman

1. Iniciar MariaDB y los cuatro servicios con credenciales configuradas solo localmente.
2. Importar `Lab11.postman_collection.json` en Postman.
3. Abrir Run collection y seleccionar las 37 solicitudes, una iteración y sin archivo de datos.
4. Mantener el orden: Categoria, Cliente, Producto, Pedido y Limpieza. No definir variables externas con los mismos nombres de URL o ID.
5. Ejecutar Run y revisar cada prueba. Guardar el resumen/exportación o capturas localmente en `evidencias/`.

| Operación | Código esperado |
|---|---:|
| POST válido | 201 y un ID positivo |
| GET lista o registro creado | 200 |
| PUT válido | 200 |
| POST con campos inválidos | 400 con errores por campo |
| GET con ID abc | 400 |
| GET de registro inexistente | 404 |
| Pedido con total incoherente | 400 |
| DELETE de registro creado por esta ejecución | 204 |
| GET después del DELETE | 404 |

URLs predeterminadas: categoría http://localhost:8082, cliente http://localhost:8081, producto http://localhost:8083 y pedido http://localhost:8084. Rutas: /api/categorias, /api/clientes, /api/productos y /api/pedidos.

La colección captura los IDs generados. El producto referencia la categoría de prueba y el pedido referencia el cliente y producto de prueba. El pedido usa cantidad 2 y precio 3500: total esperado 7000.00. La limpieza elimina pedido, producto, cliente y categoría, en ese orden.

Las solicitudes dependientes se omiten si no se obtuvo un ID propio. Una omisión no cuenta como prueba aprobada: corregir primero la creación fallida y repetir la colección completa. No sustituir IDs por datos existentes. No se necesitan contraseñas MariaDB en Postman.

Ya se realizaron 51 solicitudes REST reales desde PowerShell. Esta colección de 37 solicitudes aún debe ejecutarse mediante el Runner para obtener evidencia específica de Postman.
