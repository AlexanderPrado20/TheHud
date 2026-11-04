-- ============================================================
--  TheFHud - Migración: productos que componen un combo
--  Ejecutar una sola vez sobre la base tienda_suplementos.
--
--  Permite que al vender un combo se descuente el stock de cada
--  producto incluido (cantidad del combo x cantidad vendida).
-- ============================================================

USE tienda_suplementos;

-- Detalle de productos de cada combo.
CREATE TABLE IF NOT EXISTS combo_detalle (
    id_combo    INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad    INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_combo, id_producto),
    CONSTRAINT fk_combo_detalle_combo
        FOREIGN KEY (id_combo) REFERENCES combos(id_combo) ON DELETE CASCADE,
    CONSTRAINT fk_combo_detalle_producto
        FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Registro de combos vendidos. Se crea solo si no existía.
CREATE TABLE IF NOT EXISTS detalle_combos (
    id_detalle_combo INT NOT NULL AUTO_INCREMENT,
    id_venta         INT NOT NULL,
    id_combo         INT NOT NULL,
    cantidad         INT NOT NULL DEFAULT 1,
    precio_unitario  DECIMAL(10,2) NOT NULL DEFAULT 0,
    subtotal         DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_detalle_combo),
    KEY idx_detalle_combos_venta (id_venta),
    KEY idx_detalle_combos_combo (id_combo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Verificación rápida.
SELECT 'combo_detalle' AS tabla, COUNT(*) AS columnas
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'tienda_suplementos' AND TABLE_NAME = 'combo_detalle';
