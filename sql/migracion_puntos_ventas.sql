-- ============================================================
-- Migración: columnas de fidelidad / puntos en la tabla ventas
-- Base de datos: tienda_suplementos
-- Ejecutar UNA sola vez. Si una columna ya existe, omite esa línea.
-- ============================================================

ALTER TABLE ventas
  ADD COLUMN id_cliente INT NULL,
  ADD COLUMN puntos_usados INT NOT NULL DEFAULT 0,
  ADD COLUMN descuento_puntos DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN puntos_ganados INT NOT NULL DEFAULT 0;

ALTER TABLE ventas
  ADD INDEX idx_ventas_cliente (id_cliente);

-- Verificación:
-- SHOW COLUMNS FROM ventas;
