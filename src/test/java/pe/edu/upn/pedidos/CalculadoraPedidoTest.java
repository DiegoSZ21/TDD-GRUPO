package pe.edu.upn.pedidos;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculadoraPedidoTest {

    private final CalculadoraPedido calc = new CalculadoraPedido();

    private static BigDecimal soles(String monto) {
        return new BigDecimal(monto);
    }

    private static Producto producto(String nombre, String precio, int cantidad) {
        return new Producto(nombre, soles(precio), cantidad);
    }

    @Test
    void subtotalMultiplicaPrecioPorCantidad() {
        List<Producto> productos = List.of(
                producto("Cuaderno", "25.50", 2),
                producto("Lapicero", "10.00", 3));
        assertEquals(soles("81.00"), calc.calcularSubtotal(productos));
    }

    @Test
    void subtotalDeListaVaciaEsCero() {
        assertEquals(soles("0.00"), calc.calcularSubtotal(List.of()));
    }

    @ParameterizedTest(name = "{0} con {1}% -> {2}")
    @CsvSource({ "100.00, 10, 90.00", "100.00, 0, 100.00", "100.00, 100, 0.00" })
    void aplicarDescuentoRestaElPorcentaje(BigDecimal subtotal, BigDecimal porcentaje,
            BigDecimal esperado) {
        assertEquals(esperado, calc.aplicarDescuento(subtotal, porcentaje));

    }
}