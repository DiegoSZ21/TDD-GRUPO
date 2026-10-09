package pe.edu.upn.pedidos;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @CsvSource({ "100.00, 18.00", "90.00, 16.20" })
    void calcularImpuestoAplica18PorCiento(BigDecimal base, BigDecimal esperado) {
        assertEquals(esperado, calc.calcularImpuesto(base));
    }

    @Test
    void totalConCuponDel10PorCientoEs106_20() {
        List<Producto> productos = List.of(producto("Audifonos", "100.00", 1));
        assertEquals(soles("106.20"), calc.calcularTotal(productos, soles("10")));
    }

    @Test
    void totalRedondeaElIgvADosDecimales() {
        // subtotal 33.33 -> IGV exacto 5.9994 -> se redondea a 6.00 -> total 39.33
        List<Producto> productos = List.of(producto("Lapiz", "11.11", 3));
        assertEquals(soles("39.33"), calc.calcularTotal(productos));
    }

    @Test
    void precioNegativoLanzaExcepcion() {
        List<Producto> productos = List.of(producto("Defectuoso", "-5.00", 1));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calc.calcularSubtotal(productos));
        assertEquals("El precio no puede ser negativo", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -2 })
    void cantidadNoPositivaLanzaExcepcion(int cantidad) {
        List<Producto> productos = List.of(producto("Mouse", "50.00", cantidad));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularSubtotal(productos));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "101" })
    void descuentoFueraDeRangoLanzaExcepcion(String porcentaje) {
        assertThrows(IllegalArgumentException.class,
                () -> calc.aplicarDescuento(soles("100.00"), soles(porcentaje)));
    }
}