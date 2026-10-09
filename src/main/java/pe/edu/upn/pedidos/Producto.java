package pe.edu.upn.pedidos;

import java.math.BigDecimal;

public record Producto(String nombre, BigDecimal precio, int cantidad) {
}