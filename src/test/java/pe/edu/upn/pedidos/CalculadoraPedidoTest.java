package pe.edu.upn.pedidos;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraPedidoTest {
    private final CalculadoraPedido calc = new CalculadoraPedido();

    private static BigDecimal soles(String monto) {
        return new BigDecimal(monto);
    }

    private static Producto producto(String nombre, String precio, int cantidad) {
        return new Producto(nombre, soles(precio), cantidad);
    }

    @Test
    void subtotalDeDosProductosSumaSusPrecios() {
        List<Producto> productos = List.of(
                producto("Mouse", "50.00", 1),
                producto("Teclado", "30.00", 1));
        assertEquals(soles("80.00"), calc.calcularSubtotal(productos));
    }
}