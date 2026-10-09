package pe.edu.upn.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcula subtotal, descuento, IGV (18%) y total de un pedido.
 * Todos los montos son BigDecimal con 2 decimales (HALF_UP).
 */
public class CalculadoraPedido {

    private static final String MSG_PRECIO_NEGATIVO = "El precio no puede ser negativo";
    private static final String MSG_CANTIDAD_INVALIDA = "La cantidad debe ser mayor que cero";
    private static final String MSG_DESCUENTO_INVALIDO = "El descuento debe estar entre 0 y 100";
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    public BigDecimal calcularSubtotal(List<Producto> productos) {
        productos.forEach(this::validarProducto);
        return redondear(productos.stream()
                .map(this::importeDeLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal importeDeLinea(Producto producto) {
        return producto.precio().multiply(BigDecimal.valueOf(producto.cantidad()));
    }

    public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
        validarPorcentaje(porcentaje);
        BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
        return redondear(subtotal.subtract(descuento));
    }

    public BigDecimal calcularImpuesto(BigDecimal baseImponible) {
        return redondear(baseImponible.multiply(TASA_IGV));
    }

    private BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal(List<Producto> productos) {
        return calcularTotal(productos, BigDecimal.ZERO);
    }

    public BigDecimal calcularTotal(List<Producto> productos, BigDecimal porcentaje) {
        BigDecimal baseImponible = aplicarDescuento(calcularSubtotal(productos), porcentaje);
        return redondear(baseImponible.add(calcularImpuesto(baseImponible)));
    }

    private void validarProducto(Producto producto) {
        if (producto.precio().signum() < 0) {
            throw new IllegalArgumentException(MSG_PRECIO_NEGATIVO);
        }
        if (producto.cantidad() <= 0) {
            throw new IllegalArgumentException(MSG_CANTIDAD_INVALIDA);
        }
    }

    private void validarPorcentaje(BigDecimal porcentaje) {
        if (porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException(MSG_DESCUENTO_INVALIDO);
        }
    }
}