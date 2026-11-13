# Resultados de las verificaciones

Fecha: 13 de noviembre de 2026, America/Lima.

| Verificación | Resultado |
|---|---|
| Maven Wrapper, Java 25, Spring Boot 4.1.0 | BUILD SUCCESS en los cuatro proyectos |
| Integración HTTP/JPA con H2 | 16 pruebas; cero fallos, errores u omisiones |
| MariaDB 10.4.32 | Conexión autenticada a las cuatro bases |
| Servicios en 8081, 8082, 8083 y 8084 | HTTP 200 y base correspondiente confirmada |
| REST real sobre MariaDB | 51 solicitudes con código esperado |
| Postman Collection Runner | Preparado; no ejecutado |
| Perfil prod | No ejecutado |

Las 51 respuestas reales fueron: 18 con HTTP 200, 4 con 201, 4 con 204, 9 con 400 y 16 con 404. Los errores 400 y 404 fueron respuestas esperadas en casos negativos, no fallos de las pruebas.

Se verificó CRUD, persistencia de actualizaciones, validaciones, recursos inexistentes, total incoherente de Pedido y reemplazo de detalles. Se eliminaron exclusivamente los registros creados por la ejecución de prueba. Las cuatro bases inicialmente vacías conservaron su existencia y las tablas quedaron sin filas al finalizar la limpieza.

Los detalles originales (solicitudes, respuestas, recuentos JDBC y logs) permanecen guardados localmente. Se publica este resumen sin credenciales ni rutas personales. Aprobar H2 no sustituye la comprobación con MariaDB; ambas se realizaron por separado.
