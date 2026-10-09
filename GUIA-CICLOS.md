# Guía de ciclos TDD (copiar y pegar)
Cada bloque es el **cambio exacto** de un commit (líneas con `+` se agregan, con `-` se quitan). Usa estos pasos en cada commit:
1. Haz el cambio del bloque. 2. Ejecuta `mvn test` y toma captura. 3. `git add -A` y `git commit -m "<mensaje>"`.
Los archivos Java llevan `package pe.edu.upn.pedidos;` y van en `src/main/java/...` o `src/test/java/...` (la ruta aparece en cada bloque).

## `red: ciclo 1 - prueba de subtotal de dos productos (no compila: faltan clases)`

```diff
--- /dev/null
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -0,0 +1,20 @@
+package pe.edu.upn.pedidos;
+
+import static org.junit.jupiter.api.Assertions.*;
+
+import java.math.BigDecimal;
+import java.util.List;
+import org.junit.jupiter.api.Test;
+
+class CalculadoraPedidoTest {
+
+    private final CalculadoraPedido calc = new CalculadoraPedido();
+
+    @Test
+    void subtotalDeDosProductosSumaSusPrecios() {
+        List<Producto> productos = List.of(
+                new Producto("Mouse", new BigDecimal("50.00"), 1),
+                new Producto("Teclado", new BigDecimal("30.00"), 1));
+        assertEquals(new BigDecimal("80.00"), calc.calcularSubtotal(productos));
+    }
+}
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 1 - calcularSubtotal suma los precios (ignora cantidad)`

```diff
--- /dev/null
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -0,0 +1,13 @@
+package pe.edu.upn.pedidos;
+
+import java.math.BigDecimal;
+import java.util.List;
+
+public class CalculadoraPedido {
+
+    public BigDecimal calcularSubtotal(List<Producto> productos) {
+        return productos.stream()
+                .map(Producto::precio)
+                .reduce(BigDecimal.ZERO, BigDecimal::add);
+    }
+}
--- /dev/null
+++ b/src/main/java/pe/edu/upn/pedidos/Producto.java
@@ -0,0 +1,5 @@
+package pe.edu.upn.pedidos;
+
+import java.math.BigDecimal;
+
+public record Producto(String nombre, BigDecimal precio, int cantidad) { }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 1 - helpers soles() y producto() en la prueba`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -10,11 +10,19 @@ class CalculadoraPedidoTest {
 
     private final CalculadoraPedido calc = new CalculadoraPedido();
 
+    private static BigDecimal soles(String monto) {
+        return new BigDecimal(monto);
+    }
+
+    private static Producto producto(String nombre, String precio, int cantidad) {
+        return new Producto(nombre, soles(precio), cantidad);
+    }
+
     @Test
     void subtotalDeDosProductosSumaSusPrecios() {
         List<Producto> productos = List.of(
-                new Producto("Mouse", new BigDecimal("50.00"), 1),
-                new Producto("Teclado", new BigDecimal("30.00"), 1));
-        assertEquals(new BigDecimal("80.00"), calc.calcularSubtotal(productos));
+                producto("Mouse", "50.00", 1),
+                producto("Teclado", "30.00", 1));
+        assertEquals(soles("80.00"), calc.calcularSubtotal(productos));
     }
 }
```

## `red: ciclo 2 - pruebas de cantidad y lista vacia (fallan: 35.50 vs 81.00 y 0 vs 0.00)`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -25,4 +25,17 @@ class CalculadoraPedidoTest {
                 producto("Teclado", "30.00", 1));
         assertEquals(soles("80.00"), calc.calcularSubtotal(productos));
     }
+
+    @Test
+    void subtotalMultiplicaPrecioPorCantidad() {
+        List<Producto> productos = List.of(
+                producto("Cuaderno", "25.50", 2),
+                producto("Lapicero", "10.00", 3));
+        assertEquals(soles("81.00"), calc.calcularSubtotal(productos));
+    }
+
+    @Test
+    void subtotalDeListaVaciaEsCero() {
+        assertEquals(soles("0.00"), calc.calcularSubtotal(List.of()));
+    }
 }
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 2 - subtotal multiplica precio por cantidad y fija escala 2`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -1,13 +1,15 @@
 package pe.edu.upn.pedidos;
 
 import java.math.BigDecimal;
+import java.math.RoundingMode;
 import java.util.List;
 
 public class CalculadoraPedido {
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
         return productos.stream()
-                .map(Producto::precio)
-                .reduce(BigDecimal.ZERO, BigDecimal::add);
+                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.cantidad())))
+                .reduce(BigDecimal.ZERO, BigDecimal::add)
+                .setScale(2, RoundingMode.HALF_UP);
     }
 }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 2 - extraer importeDeLinea() para nombrar el calculo precio x cantidad`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -8,8 +8,12 @@ public class CalculadoraPedido {
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
         return productos.stream()
-                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.cantidad())))
+                .map(this::importeDeLinea)
                 .reduce(BigDecimal.ZERO, BigDecimal::add)
                 .setScale(2, RoundingMode.HALF_UP);
     }
+
+    private BigDecimal importeDeLinea(Producto producto) {
+        return producto.precio().multiply(BigDecimal.valueOf(producto.cantidad()));
+    }
 }
```

## `red: ciclo 3 - prueba parametrizada de descuento (no compila: aplicarDescuento no existe)`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -5,6 +5,8 @@ import static org.junit.jupiter.api.Assertions.*;
 import java.math.BigDecimal;
 import java.util.List;
 import org.junit.jupiter.api.Test;
+import org.junit.jupiter.params.provider.CsvSource;
+import org.junit.jupiter.params.ParameterizedTest;
 
 class CalculadoraPedidoTest {
 
@@ -38,4 +40,11 @@ class CalculadoraPedidoTest {
     void subtotalDeListaVaciaEsCero() {
         assertEquals(soles("0.00"), calc.calcularSubtotal(List.of()));
     }
+
+    @ParameterizedTest(name = "{0} con {1}% -> {2}")
+    @CsvSource({ "100.00, 10, 90.00", "100.00, 0, 100.00", "100.00, 100, 0.00" })
+    void aplicarDescuentoRestaElPorcentaje(BigDecimal subtotal, BigDecimal porcentaje,
+                                           BigDecimal esperado) {
+        assertEquals(esperado, calc.aplicarDescuento(subtotal, porcentaje));
+    }
 }
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 3 - aplicarDescuento resta el porcentaje al subtotal`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -16,4 +16,9 @@ public class CalculadoraPedido {
     private BigDecimal importeDeLinea(Producto producto) {
         return producto.precio().multiply(BigDecimal.valueOf(producto.cantidad()));
     }
+
+    public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
+        BigDecimal descuento = subtotal.multiply(porcentaje).divide(new BigDecimal("100"));
+        return subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
+    }
 }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 3 - constante CIEN en lugar del literal repetido`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -6,6 +6,8 @@ import java.util.List;
 
 public class CalculadoraPedido {
 
+    private static final BigDecimal CIEN = new BigDecimal("100");
+
     public BigDecimal calcularSubtotal(List<Producto> productos) {
         return productos.stream()
                 .map(this::importeDeLinea)
@@ -18,7 +20,7 @@ public class CalculadoraPedido {
     }
 
     public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
-        BigDecimal descuento = subtotal.multiply(porcentaje).divide(new BigDecimal("100"));
+        BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
         return subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
     }
 }
```

## `red: ciclo 4 - prueba parametrizada de IGV (no compila: calcularImpuesto no existe)`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -47,4 +47,10 @@ class CalculadoraPedidoTest {
                                            BigDecimal esperado) {
         assertEquals(esperado, calc.aplicarDescuento(subtotal, porcentaje));
     }
+
+    @ParameterizedTest
+    @CsvSource({ "100.00, 18.00", "90.00, 16.20" })
+    void calcularImpuestoAplica18PorCiento(BigDecimal base, BigDecimal esperado) {
+        assertEquals(esperado, calc.calcularImpuesto(base));
+    }
 }
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 4 - calcularImpuesto aplica TASA_IGV 0.18 con 2 decimales`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -7,6 +7,7 @@ import java.util.List;
 public class CalculadoraPedido {
 
     private static final BigDecimal CIEN = new BigDecimal("100");
+    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
         return productos.stream()
@@ -23,4 +24,8 @@ public class CalculadoraPedido {
         BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
         return subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
     }
+
+    public BigDecimal calcularImpuesto(BigDecimal baseImponible) {
+        return baseImponible.multiply(TASA_IGV).setScale(2, RoundingMode.HALF_UP);
+    }
 }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 4 - extraer redondear() para no repetir setScale`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -10,10 +10,10 @@ public class CalculadoraPedido {
     private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
-        return productos.stream()
+        return redondear(productos.stream()
                 .map(this::importeDeLinea)
-                .reduce(BigDecimal.ZERO, BigDecimal::add)
-                .setScale(2, RoundingMode.HALF_UP);
+                .reduce(BigDecimal.ZERO, BigDecimal::add))
+                ;
     }
 
     private BigDecimal importeDeLinea(Producto producto) {
@@ -22,10 +22,14 @@ public class CalculadoraPedido {
 
     public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
         BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
-        return subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
+        return redondear(subtotal.subtract(descuento));
     }
 
     public BigDecimal calcularImpuesto(BigDecimal baseImponible) {
-        return baseImponible.multiply(TASA_IGV).setScale(2, RoundingMode.HALF_UP);
+        return redondear(baseImponible.multiply(TASA_IGV));
+    }
+
+    private BigDecimal redondear(BigDecimal monto) {
+        return monto.setScale(2, RoundingMode.HALF_UP);
     }
 }
```

## `red: ciclo 5 - pruebas de total y redondeo (no compila: calcularTotal no existe)`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -53,4 +53,17 @@ class CalculadoraPedidoTest {
     void calcularImpuestoAplica18PorCiento(BigDecimal base, BigDecimal esperado) {
         assertEquals(esperado, calc.calcularImpuesto(base));
     }
+
+    @Test
+    void totalConCuponDel10PorCientoEs106_20() {
+        List<Producto> productos = List.of(producto("Audifonos", "100.00", 1));
+        assertEquals(soles("106.20"), calc.calcularTotal(productos, soles("10")));
+    }
+
+    @Test
+    void totalRedondeaElIgvADosDecimales() {
+        // subtotal 33.33 -> IGV exacto 5.9994 -> se redondea a 6.00 -> total 39.33
+        List<Producto> productos = List.of(producto("Lapiz", "11.11", 3));
+        assertEquals(soles("39.33"), calc.calcularTotal(productos, soles("0")));
+    }
 }
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 5 - calcularTotal combina subtotal, descuento e IGV con HALF_UP`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -32,4 +32,11 @@ public class CalculadoraPedido {
     private BigDecimal redondear(BigDecimal monto) {
         return monto.setScale(2, RoundingMode.HALF_UP);
     }
+
+    public BigDecimal calcularTotal(List<Producto> productos, BigDecimal porcentaje) {
+        BigDecimal subtotal = calcularSubtotal(productos);
+        BigDecimal baseImponible = aplicarDescuento(subtotal, porcentaje);
+        BigDecimal igv = calcularImpuesto(baseImponible);
+        return baseImponible.add(igv).setScale(2, RoundingMode.HALF_UP);
+    }
 }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 5 - calcularTotal reutiliza redondear() y se simplifica`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -34,9 +34,7 @@ public class CalculadoraPedido {
     }
 
     public BigDecimal calcularTotal(List<Producto> productos, BigDecimal porcentaje) {
-        BigDecimal subtotal = calcularSubtotal(productos);
-        BigDecimal baseImponible = aplicarDescuento(subtotal, porcentaje);
-        BigDecimal igv = calcularImpuesto(baseImponible);
-        return baseImponible.add(igv).setScale(2, RoundingMode.HALF_UP);
+        BigDecimal baseImponible = aplicarDescuento(calcularSubtotal(productos), porcentaje);
+        return redondear(baseImponible.add(calcularImpuesto(baseImponible)));
     }
 }
```

## `red: ciclo 6 - pruebas de validaciones (fallan: no se lanza IllegalArgumentException)`

```diff
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -5,6 +5,7 @@ import static org.junit.jupiter.api.Assertions.*;
 import java.math.BigDecimal;
 import java.util.List;
 import org.junit.jupiter.api.Test;
+import org.junit.jupiter.params.provider.ValueSource;
 import org.junit.jupiter.params.provider.CsvSource;
 import org.junit.jupiter.params.ParameterizedTest;
 
@@ -66,4 +67,26 @@ class CalculadoraPedidoTest {
         List<Producto> productos = List.of(producto("Lapiz", "11.11", 3));
         assertEquals(soles("39.33"), calc.calcularTotal(productos, soles("0")));
     }
+
+    @Test
+    void precioNegativoLanzaExcepcion() {
+        List<Producto> productos = List.of(producto("Defectuoso", "-5.00", 1));
+        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
+                () -> calc.calcularSubtotal(productos));
+        assertEquals("El precio no puede ser negativo", ex.getMessage());
+    }
+
+    @ParameterizedTest
+    @ValueSource(ints = { 0, -2 })
+    void cantidadNoPositivaLanzaExcepcion(int cantidad) {
+        List<Producto> productos = List.of(producto("Mouse", "50.00", cantidad));
+        assertThrows(IllegalArgumentException.class, () -> calc.calcularSubtotal(productos));
+    }
+
+    @ParameterizedTest
+    @ValueSource(strings = { "-1", "101" })
+    void descuentoFueraDeRangoLanzaExcepcion(String porcentaje) {
+        assertThrows(IllegalArgumentException.class,
+                () -> calc.aplicarDescuento(soles("100.00"), soles(porcentaje)));
+    }
 }
```
Resultado esperado: **falla / no compila** (captura).

## `green: ciclo 6 - validaciones de precio, cantidad y porcentaje de descuento`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -10,6 +10,14 @@ public class CalculadoraPedido {
     private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
+        for (Producto p : productos) {
+            if (p.precio().signum() < 0) {
+                throw new IllegalArgumentException("El precio no puede ser negativo");
+            }
+            if (p.cantidad() <= 0) {
+                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
+            }
+        }
         return redondear(productos.stream()
                 .map(this::importeDeLinea)
                 .reduce(BigDecimal.ZERO, BigDecimal::add))
@@ -21,6 +29,9 @@ public class CalculadoraPedido {
     }
 
     public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
+        if (porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) > 0) {
+            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
+        }
         BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
         return redondear(subtotal.subtract(descuento));
     }
```
Resultado esperado: **todas pasan** (captura).

## `refactor: ciclo 6 - extraer validarProducto() y validarPorcentaje()`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -10,14 +10,7 @@ public class CalculadoraPedido {
     private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
-        for (Producto p : productos) {
-            if (p.precio().signum() < 0) {
-                throw new IllegalArgumentException("El precio no puede ser negativo");
-            }
-            if (p.cantidad() <= 0) {
-                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
-            }
-        }
+        productos.forEach(this::validarProducto);
         return redondear(productos.stream()
                 .map(this::importeDeLinea)
                 .reduce(BigDecimal.ZERO, BigDecimal::add))
@@ -29,9 +22,7 @@ public class CalculadoraPedido {
     }
 
     public BigDecimal aplicarDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
-        if (porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) > 0) {
-            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
-        }
+        validarPorcentaje(porcentaje);
         BigDecimal descuento = subtotal.multiply(porcentaje).divide(CIEN);
         return redondear(subtotal.subtract(descuento));
     }
@@ -48,4 +39,19 @@ public class CalculadoraPedido {
         BigDecimal baseImponible = aplicarDescuento(calcularSubtotal(productos), porcentaje);
         return redondear(baseImponible.add(calcularImpuesto(baseImponible)));
     }
+
+    private void validarProducto(Producto producto) {
+        if (producto.precio().signum() < 0) {
+            throw new IllegalArgumentException("El precio no puede ser negativo");
+        }
+        if (producto.cantidad() <= 0) {
+            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
+        }
+    }
+
+    private void validarPorcentaje(BigDecimal porcentaje) {
+        if (porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) > 0) {
+            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
+        }
+    }
 }
```

## `refactor: ciclo 7 - sobrecarga calcularTotal(productos) sin cupon delega con porcentaje 0`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -35,6 +35,10 @@ public class CalculadoraPedido {
         return monto.setScale(2, RoundingMode.HALF_UP);
     }
 
+    public BigDecimal calcularTotal(List<Producto> productos) {
+        return calcularTotal(productos, BigDecimal.ZERO);
+    }
+
     public BigDecimal calcularTotal(List<Producto> productos, BigDecimal porcentaje) {
         BigDecimal baseImponible = aplicarDescuento(calcularSubtotal(productos), porcentaje);
         return redondear(baseImponible.add(calcularImpuesto(baseImponible)));
--- a/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
+++ b/src/test/java/pe/edu/upn/pedidos/CalculadoraPedidoTest.java
@@ -65,7 +65,7 @@ class CalculadoraPedidoTest {
     void totalRedondeaElIgvADosDecimales() {
         // subtotal 33.33 -> IGV exacto 5.9994 -> se redondea a 6.00 -> total 39.33
         List<Producto> productos = List.of(producto("Lapiz", "11.11", 3));
-        assertEquals(soles("39.33"), calc.calcularTotal(productos, soles("0")));
+        assertEquals(soles("39.33"), calc.calcularTotal(productos));
     }
 
     @Test
```

## `refactor: ciclo 7 - constantes para mensajes de error y Javadoc de la clase`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -4,8 +4,15 @@ import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 
+/**
+ * Calcula subtotal, descuento, IGV (18%) y total de un pedido.
+ * Todos los montos son BigDecimal con 2 decimales (HALF_UP).
+ */
 public class CalculadoraPedido {
 
+    private static final String MSG_PRECIO_NEGATIVO = "El precio no puede ser negativo";
+    private static final String MSG_CANTIDAD_INVALIDA = "La cantidad debe ser mayor que cero";
+    private static final String MSG_DESCUENTO_INVALIDO = "El descuento debe estar entre 0 y 100";
     private static final BigDecimal CIEN = new BigDecimal("100");
     private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
 
@@ -46,16 +53,16 @@ public class CalculadoraPedido {
 
     private void validarProducto(Producto producto) {
         if (producto.precio().signum() < 0) {
-            throw new IllegalArgumentException("El precio no puede ser negativo");
+            throw new IllegalArgumentException(MSG_PRECIO_NEGATIVO);
         }
         if (producto.cantidad() <= 0) {
-            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
+            throw new IllegalArgumentException(MSG_CANTIDAD_INVALIDA);
         }
     }
 
     private void validarPorcentaje(BigDecimal porcentaje) {
         if (porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) > 0) {
-            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
+            throw new IllegalArgumentException(MSG_DESCUENTO_INVALIDO);
         }
     }
 }
```

## `refactor: ciclo 7 - calcularSubtotal con variable intermedia legible`

```diff
--- a/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
+++ b/src/main/java/pe/edu/upn/pedidos/CalculadoraPedido.java
@@ -18,10 +18,10 @@ public class CalculadoraPedido {
 
     public BigDecimal calcularSubtotal(List<Producto> productos) {
         productos.forEach(this::validarProducto);
-        return redondear(productos.stream()
+        BigDecimal suma = productos.stream()
                 .map(this::importeDeLinea)
-                .reduce(BigDecimal.ZERO, BigDecimal::add))
-                ;
+                .reduce(BigDecimal.ZERO, BigDecimal::add);
+        return redondear(suma);
     }
 
     private BigDecimal importeDeLinea(Producto producto) {
```

## `docs: bitacora de ciclos y respuestas de reflexion`

```diff
--- /dev/null
+++ b/BITACORA.md
@@ -0,0 +1,25 @@
+# Bitácora de ciclos y reflexión
+
+## Bitácora (sección 6)
+
+| Ciclo | Prueba escrita | Red: ¿por qué falló? | Green: ¿qué cambiaste? | Refactor realizado |
+|---|---|---|---|---|
+| 1 | subtotalDeDosProductosSumaSusPrecios | No compilaba: Producto y CalculadoraPedido no existían | Se creó calcularSubtotal sumando precios | Métodos soles() y producto() en la prueba |
+| 2 | subtotalMultiplicaPrecioPorCantidad, subtotalDeListaVaciaEsCero | Devolvía 35.50 en vez de 81.00; la lista vacía devolvía "0" en vez de "0.00" (escala) | Se multiplicó precio × cantidad y se aplicó setScale(2, HALF_UP) | Se extrajo importeDeLinea() |
+| 3 | aplicarDescuentoRestaElPorcentaje (3 filas @CsvSource) | No compilaba: aplicarDescuento no existía | Se creó aplicarDescuento: subtotal − subtotal×porcentaje/100 | Constante CIEN |
+| 4 | calcularImpuestoAplica18PorCiento (2 filas @CsvSource) | No compilaba: calcularImpuesto no existía | Se creó calcularImpuesto con TASA_IGV = 0.18 y escala 2 | Método redondear() para no repetir setScale |
+| 5 | totalConCuponDel10PorCientoEs106_20, totalRedondeaElIgvADosDecimales | No compilaba: calcularTotal no existía | calcularTotal = base imponible + IGV, redondeado HALF_UP | calcularTotal reutiliza redondear() y se simplifica |
+| 6 | precioNegativoLanzaExcepcion, cantidadNoPositivaLanzaExcepcion (0, −2), descuentoFueraDeRangoLanzaExcepcion (−1, 101) | Fallaban 5 casos: no se lanzaba IllegalArgumentException | Validaciones al inicio de calcularSubtotal y aplicarDescuento | Extracción de validarProducto() y validarPorcentaje() |
+| 7 | — | — | — | (1) Sobrecarga calcularTotal(productos) sin cupón que delega con 0; (2) constantes MSG_* para los mensajes; (3) Javadoc y variable intermedia en calcularSubtotal |
+
+## Preguntas de reflexión (sección 7)
+
+1. **Datos de prueba.** El código ignoraba la cantidad y la prueba pasaba porque ambas cantidades eran 1 (1 × precio = precio). Una prueba solo detecta defectos que sus datos pueden revelar: hay que elegir datos que distingan la implementación correcta de una incorrecta (cantidades distintas de 1, valores diferentes entre sí). Por eso el ciclo 2 usa 25.50 × 2 y 10.00 × 3.
+
+2. **BigDecimal y String.** double usa punto flotante binario y no representa exactamente muchos decimales (0.1 + 0.2 = 0.30000000000000004), lo que genera errores de centésimas en dinero. BigDecimal guarda el valor decimal exacto. Se crea desde String porque `new BigDecimal(0.1)` ya recibe un double impreciso (0.1000000000000000055…), mientras que `new BigDecimal("0.1")` es exacto.
+
+3. **Escala.** En el ciclo 2, la lista vacía devolvía BigDecimal.ZERO (escala 0, "0") y la prueba esperaba "0.00". En el ciclo 4, 90.00 × 0.18 da 16.2000 (escala 4) y se esperaba 16.20. `equals` compara valor y escala, así que 16.2000 ≠ 16.20. La alternativa es `compareTo`, que compara solo el valor numérico (`assertEquals(0, esperado.compareTo(actual))`). En este caso se resolvió con setScale(2, HALF_UP).
+
+4. **Valores límite.** −1, 0, 100 y 101. 0 y 100 son los límites válidos (sin descuento y descuento total, ciclo 3); −1 y 101 son los valores inmediatamente fuera del rango (ciclo 6). Los errores suelen ocurrir justo en los bordes (por ejemplo `<` en vez de `<=`). También se probó cantidad 0 y −2 (límite y valor inválido) para las cantidades.
+
+5. **Productos exonerados de IGV.** Primero escribiría pruebas como: un pedido con solo productos exonerados tiene IGV 0.00; un pedido mixto aplica IGV solo a los no exonerados; un producto exonerado con descuento sigue sin IGV. En el diseño, Producto necesitaría un atributo (por ejemplo `boolean exonerado`), y calcularImpuesto dejaría de recibir una sola base: tendría que calcular la base afecta y la exonerada por separado, y calcularTotal tendría que combinarlas.
```
