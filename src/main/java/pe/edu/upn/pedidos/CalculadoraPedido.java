package pe.edu.upn.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculadoraPedido {

    public BigDecimal calcularSubtotal(List<Producto> productos) {
        return productos.stream()

                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}