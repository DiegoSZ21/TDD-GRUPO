package pe.edu.upn.pedidos;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;

class CalculadoraPedidoTest {
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