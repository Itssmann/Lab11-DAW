-- Solo lectura. Consulta ejecutada en la sesión abierta de SQLyog.
SELECT VERSION() AS version_servidor,
       @@port AS puerto,
       CURRENT_USER() AS cuenta,
       SCHEMA_NAME AS base_verificada
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ('bd_categoria','bd_cliente','bd_producto','bd_pedido')
ORDER BY SCHEMA_NAME;
