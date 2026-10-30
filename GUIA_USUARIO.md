# 🛒 Guía del Usuario - Carrito de Compras INNOVA SUR

## Introducción

¡Bienvenido al sistema de carrito de compras de INNOVA SUR! Esta guía te mostrará cómo usar las herramientas para comparar, construir y guardar configuraciones de sofás, así como crear pedidos.

### ¿Qué puedes hacer?

- **👀 Comparador**: Visualiza hasta 3 sofás lado a lado
- **🔨 Constructor**: Arma sofás pieza por pieza
- **🛒 Carrito**: Guarda múltiples configuraciones
- **💰 Precios**: Cálculo automático en tiempo real
- **📦 Pedidos**: Crea órdenes automáticas
- **📄 PDF**: Genera presupuestos

---

## 1. Flujo General

```
Comparador  →  Constructor  →  Carrito  →  Pedidos
   (mirar)      (armar)      (guardar)   (crear orden)
```

**Nota:** No es obligatorio usar todos. Puedes ir directo de Comparador a Carrito.

---

## 2. Comparador A/B/C

### ¿Para qué sirve?
Compara hasta 3 sofás diferentes lado a lado con rotación sincronizada.

### Pasos:
1. Abre el módulo **Comparador** desde el menú **CATÁLOGO**
2. Verás 3 opciones:
   - **Opción A**: Carga con "Moderno 3 cuerpos" por defecto
   - **Opción B**: Vacía (puedes cargar otra configuración)
   - **Opción C**: Vacía (puedes cargar otra configuración)
3. **Rota** cualquier sofá con el ratón → todos rotan automáticamente
4. Presiona **Agregar al carrito** bajo la opción que desees

### Características:
- ✅ Sincronización de rotación automática
- ✅ Tabla comparativa en la parte inferior
- ✅ Guardado automático con nombre "Comparador - [Modelo]"

---

## 3. Constructor Modular

### ¿Para qué sirve?
Arma sofás pieza por pieza desde módulos disponibles.

### Módulos disponibles:
| Módulo | Descripción | Ancho |
|--------|-------------|-------|
| Brazo izq + 1 cuerpo | Base izquierda con apoyabrazos | 52 cm |
| Central (sin brazos) | Sección central expandible | 52 cm |
| Esquinero | Rincón a 90° | 52 cm |
| Chaise | Longue de descanso | 52 cm |
| Puff | Asiento flotante | 40 cm |
| Brazo der + 1 cuerpo | Base derecha con apoyabrazos | 52 cm |

### Pasos:
1. Abre **Constructor modular** desde **CATÁLOGO**
2. Presiona botones en la **paleta** para agregar módulos
3. Verás en tiempo real:
   - Lista de módulos armados
   - Vista 3D actualizada
   - Dimensiones (Largo × Fondo)
4. Presiona **Guardar al carrito**

### ⚠️ Importante:
Cuando guardas, la lista de módulos se limpia para empezar una nueva configuración.

---

## 4. Carrito de Compras

### ¿Qué ves?
- **Tabla**: Configuración | Cantidad | Precio Unit. | Total | Acciones
- **Totales**: N items (M unidades) | Total: S/ X,XXX.XX
- **Botones**: Vaciar | Crear Pedidos | Descargar PDF

### Acciones:

#### ✏️ Editar Cantidad
1. Haz clic en **Editar** en la columna Acciones
2. Cambia la cantidad (1-100)
3. Presiona **OK**

#### 🗑️ Eliminar Item
1. Haz clic en **Quitar** en la columna Acciones
2. ¡Listo! Se elimina inmediatamente

#### 🧹 Vaciar Todo
1. Presiona **Vaciar Carrito**
2. Confirma en el diálogo

### Cálculo de Precios

Los precios incluyen:
- **BOM**: Costo de materiales (varía por modelo)
- **M.O.**: Mano de obra a S/ 45/hora
- **Recargas**: Por espuma, patas, mecanismo, tela premium, chaise
- **Margen**: 40% sobre costo
- **Descuento**: Si aplica
- **IGV**: 18% (incluido en total)

**Ejemplo:**
```
Moderno 3 cuerpos, Chenille, Patas metal negro
BOM: S/ 1,200 + M.O.: S/ 135 + Recargos: S/ 136 = S/ 1,471
Con margen 40%: S/ 2,060
Con IGV 18%: S/ 2,430 ← TOTAL FINAL
```

---

## 5. Creación de Pedidos

### Pasos:
1. Asegúrate de tener **al menos 1 item** en el carrito
2. Presiona **Crear Pedidos**
3. El sistema crea **1 Pedido por cada item**
4. Cada pedido recibe:
   - Código único: `PED-YYYYMM-XXXXX` (ej: PED-202610-00001)
   - Estado: `CONFIRMADO`
   - Precio y especificaciones
5. El carrito se **vacía automáticamente**

### Ver Pedidos Creados:
Abre el módulo **PEDIDOS** en la sección **COMERCIAL** del menú.

### Información Guardada:
| Campo | Ejemplo |
|-------|---------|
| Código | PED-202610-00001 |
| Cliente | Cliente desde Carrito |
| Tipo | PERSONALIZADO |
| Estado | CONFIRMADO |
| Cantidad | 1, 2, 3... |
| Precio | S/ 2,430.00 |
| Largo/Fondo/Alto | Desde configuración |
| Especificación | Modelo, cuerpos, espuma, patas, etc. |

---

## 6. Generar PDF

### Pasos:
1. Abre el **Carrito**
2. Presiona **Descargar PDF**
3. Se genera un presupuesto con:
   - Especificaciones completas
   - Todos los items del carrito
   - Totales y precios

### Usos:
- 📧 Enviar al cliente
- 🖨️ Imprimir
- 📁 Guardar en archivo

---

## ❓ Preguntas Frecuentes

### ¿Puedo cambiar cantidad después de agregar?
✅ **Sí.** Haz clic en "Editar" en la fila.

### ¿Se pierden las configuraciones al salir?
❌ **No.** Se guardan en base de datos. Verás el mismo carrito al volver.

### ¿Hay límite de items?
❌ **No.** Puedes tener ilimitados items.

### ¿El precio incluye IGV?
✅ **Sí.** El 18% está incluido.

### ¿Puedo duplicar una configuración?
❌ **No directamente.** Agrégala nuevamente desde Comparador o Constructor.

### ¿Cómo edito un pedido después?
✅ **Abre PEDIDOS** en el menú COMERCIAL. Puedes cambiar estado, agregar adelanto, etc.

### ¿Hay descuentos automáticos?
✅ **Sí, si están configurados.** Se reflejan en el total.

### ¿Qué pasa con cliente genérico?
📌 Puedes asignarlo manualmente en PEDIDOS.

---

## 📞 Soporte

Si encuentras problemas o tienes preguntas:

- 📧 **Email**: soporte@innovasur.pe
- 📱 **Teléfono**: +51 969 123456
- 🏢 **Ubicación**: Juliaca, Puno, Perú
- ⏰ **Horario**: Lunes a Viernes, 9:00 - 17:00

---

## 🎯 Resumen

| Paso | Módulo | Acción | Resultado |
|------|--------|--------|-----------|
| 1 | Comparador | Compara 3 sofás | Elige el mejor |
| 2 | Constructor | Arma módulos | Crea personalizado |
| 3 | Carrito | Guarda configuraciones | Lista con precios |
| 4 | Carrito | Ajusta cantidades | Totales en tiempo real |
| 5 | Carrito | Crea Pedidos | Órdenes automáticas |
| 6 | Carrito | Descarga PDF | Presupuesto listo |

---

## 📊 Estadísticas del Sistema

- ✅ **114 Tests** pasando
- ✅ **Compilación** limpia
- ✅ **9 Servicios** integrados
- ✅ **Cálculo dinámico** de precios
- ✅ **Persistencia** en base de datos
- ✅ **Generación** de PDF

---

**Versión**: 1.0  
**Última actualización**: Octubre 2026  
**© 2026 INNOVA SUR - Todos los derechos reservados**
