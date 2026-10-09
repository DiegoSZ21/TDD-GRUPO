package pe.edu.upn.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculadoraPedido {
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    public BigDecimal calcularSubtotal(List<Producto> productos) {
        return redondear(productos.stream()
                .map(this::importeDeLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal importeDeLinea(Producto producto) {
        return producto.precio().multiply(BigDecimal.valueOf(producto.cantidad()));
    }

    public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
        BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
        return redondear(subtotal.subtract(descuento));
    }

    public BigDecimal calcularImpuesto(BigDecimal baseImponible) {
        return redondear(baseImponible.multiply(TASA_IGV));
    }

    private BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal(List<Producto> productos, BigDecimal porcentaje) {
        BigDecimal subtotal = calcularSubtotal(productos);
        BigDecimal baseImponible = aplicarDescuento(subtotal, porcentaje);
        BigDecimal igv = calcularImpuesto(baseImponible);
        return baseImponible.add(igv).setScale(2, RoundingMode.HALF_UP);
    }

}
