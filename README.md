# Laboratorio 11 — Microservicios

Laboratorio de Desarrollo de Aplicaciones Web de Tecsup: cuatro microservicios independientes con Spring Boot 4.1.0, Java 25, Spring Web MVC, JPA, Validation, DevTools y MariaDB Connector/J. Cada proyecto contiene Maven Wrapper; el POM raíz permite compilar todos juntos.

| Servicio | Puerto dev | Base | API |
|---|---:|---|---|
| cliente-service | 8081 | bd_cliente | /api/clientes |
| categoria-service | 8082 | bd_categoria | /api/categorias |
| producto-service | 8083 | bd_producto | /api/productos |
| pedido-service | 8084 | bd_pedido | /api/pedidos |

## Requisitos y compilación

Instalar JDK 25 y configurar `JAVA_HOME` con su directorio. Tener MariaDB funcionando en localhost:3306. No se necesita Maven global.

```powershell
git clone https://github.com/Itssmann/Lab11-DAW.git
cd Lab11-DAW
.\mvnw.cmd verify --batch-mode --no-transfer-progress
```

En Linux/macOS: `sh mvnw verify`. La compilación descarga dependencias desde Maven Central. Las pruebas usan H2 efímero y no necesitan credenciales de MariaDB. Los JAR ejecutables se generan en `servicio/target/`.

En IntelliJ, importar el POM raíz, seleccionar JDK 25 y Maven Wrapper. No incluir credenciales en configuraciones compartidas.

## Ejecutar con MariaDB

Las cuatro bases deben existir. El proyecto no contiene scripts para borrarlas o recrearlas. Configurar una cuenta local con contraseña no vacía y permisos sobre esas bases, incluida la creación/actualización de tablas para el perfil dev.

Abrir cuatro terminales PowerShell en la raíz y ejecutar un comando por terminal:

```powershell
.\ejecutar.ps1 -Servicio cliente
.\ejecutar.ps1 -Servicio categoria
.\ejecutar.ps1 -Servicio producto
.\ejecutar.ps1 -Servicio pedido
```

El script solicita usuario y contraseña de forma local; la contraseña se introduce oculta, se pasa por entorno al proceso Java y no se escribe en archivos ni argumentos. Si la política de PowerShell impide ejecutar scripts, consultar al administrador del equipo; no se requiere cambiar permanentemente esa política.

Variables: `DB_USERNAME` y `DB_PASSWORD` obligatorias, `DB_HOST` (localhost), `DB_PORT` (3306), `SERVER_PORT` opcional. Para ejecutar directamente desde IntelliJ o un JAR, proporcionar estas variables solo localmente. Spring no carga archivos `.env` automáticamente.

`application.properties` define nombre y perfil predeterminado dev. `application.yml` define conexión y JPA. `application-dev.yml` utiliza Hibernate `update`: puede crear/actualizar tablas dentro de las bases existentes. `application-prod.yml` utiliza `validate`, requiere `DB_URL` y usa puertos 9091–9094 (sobrescribibles con `SERVER_PORT`). No se crean variantes de bases `_dev`.

Para un JAR, usar el ejecutable Java del JDK 25 y las variables de entorno configuradas. Detener cada servicio con Ctrl+C.

## API y validaciones

Cada recurso tiene GET lista, GET /{id}, POST (201 y Location), PUT /{id} (200) y DELETE /{id} (204). Datos, JSON o parámetros inválidos devuelven 400; registros inexistentes, 404; conflictos de integridad, 409.

- Categoría: nombre obligatorio y estado ACTIVO/INACTIVO.
- Cliente: nombre, apellidos, dirección, teléfono de 9–15 dígitos, DNI de 8 dígitos y estado.
- Producto: nombre, precio positivo de hasta dos decimales, stock no negativo y categoriaId positivo.
- Pedido: fecha ISO, idCliente y una lista no vacía de detalles (idProducto, cantidad y precio positivos). Calcula total con BigDecimal; si se envía total, debe coincidir con los detalles.

Pedido y DetallePedido tienen relación OneToMany/ManyToOne, cascada y eliminación de huérfanos. El JSON omite la referencia inversa para evitar recursión. Los IDs entre microservicios son referencias independientes; no se implementan FK entre bases ni validación remota de existencia.

```json
{"fecha":"2026-06-10","idCliente":1,"total":3679.80,"detalles":[{"idProducto":1,"cantidad":1,"precio":3500.00},{"idProducto":2,"cantidad":2,"precio":89.90}]}
```

## Postman y verificación real

Importar [la colección](postman/Lab11.postman_collection.json) y seguir [sus instrucciones](postman/INSTRUCCIONES.md). Ejecutar las 37 solicitudes en orden: Categoría, Cliente, Producto, Pedido y Limpieza. Captura los IDs creados y elimina únicamente sus propios registros de prueba.

Después de compilar, puede ejecutarse la verificación automatizada en Windows:

```powershell
.\verificacion\verificar-local.ps1 -Usuario lab11 -EntradaGrafica
```

Requiere una cuenta existente con contraseña, las cuatro bases y los puertos libres. Utiliza la caché estándar de Maven en `%USERPROFILE%/.m2/repository`. Solicita la contraseña localmente, comprueba las bases por JDBC, ejecuta los cuatro servicios y realiza 51 solicitudes REST. Guarda resultados en `evidencias/`, excluido de Git. Los servicios permanecen activos al terminar; no repetir el arranque mientras sus puertos estén ocupados.

## Resultados comprobados

El 13 de noviembre de 2026 se completaron 16 pruebas de integración con H2 y 51 solicitudes REST reales con MariaDB 10.4.32, además de generar los cuatro JAR. Véase [el resumen de resultados](RESULTADOS.md). El Runner de Postman y el perfil prod no se presentan como ejecutados.

El repositorio incluye fuentes, pruebas, configuración sin secretos y scripts. Las evidencias detalladas, logs, herramientas instaladas, binarios compilados y credenciales permanecen fuera de Git.
