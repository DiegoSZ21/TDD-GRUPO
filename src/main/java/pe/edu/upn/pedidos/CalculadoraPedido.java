package pe.edu.upn.pedidos;

import java.math.BigDecimal;
import java.util.List;

public class CalculadoraPedido {

    public BigDecimal calcularSubtotal(List<Producto> productos) {
        return productos.stream()
                .map(Producto::precio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}