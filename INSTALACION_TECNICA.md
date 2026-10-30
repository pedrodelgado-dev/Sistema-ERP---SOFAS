# 🔧 Guía de Instalación Técnica - SISTEMA ERP SOFAS

## Requisitos del Sistema

### Software
- **Java 21** o superior
- **Maven 3.9+**
- **SQLite 3.46+** (incluido en el proyecto)
- **JavaFX 21**

### Hardware
- **CPU**: 2+ cores recomendado
- **RAM**: 4GB mínimo, 8GB recomendado
- **Disco**: 500MB libre
- **Pantalla**: 1280×720 mínimo (1920×1080 recomendado)

### SO Soportados
- ✅ Windows 10/11
- ✅ macOS 11+
- ✅ Linux (Ubuntu 20.04+, Fedora 35+)

---

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/pedrodelgado-dev/Sistema-de-Muebles-Exclusivo-Sofas.git
cd sistema-erp-sofas
```

### 2. Compilar

```bash
./mvnw clean compile
```

Esto:
- Descarga todas las dependencias
- Compila el código fuente
- Genera bytecode Java

### 3. Ejecutar Tests

```bash
./mvnw test
```

Debe mostrar:
```
Tests run: 114, Failures: 0, Errors: 0
BUILD SUCCESS
```

### 4. Ejecutar Aplicación

#### Opción A: Maven
```bash
./mvnw clean javafx:run
```

#### Opción B: JAR (Después de compilar)
```bash
java -jar target/sistema-erp-sofas-1.0.0.jar
```

#### Opción C: IDE (IntelliJ IDEA / Eclipse)
1. Abre el proyecto en tu IDE
2. Localiza `Launcher.java` en `pe.erp.sofas`
3. Presiona Ctrl+Shift+F10 (IntelliJ) o Run → Run (Eclipse)

---

## Estructura del Proyecto

```
innova-sur-sofas/
├── src/
│   ├── main/
│   │   ├── java/pe/innovasur/sofas/
│   │   │   ├── core/          # Interfaces y utilidades
│   │   │   ├── model/         # Entidades JPA (Usuario, Pedido, etc.)
│   │   │   ├── repository/    # Acceso a datos
│   │   │   ├── service/       # Lógica de negocio
│   │   │   │   ├── GestorCarritoService
│   │   │   │   ├── CalculoPrecioService
│   │   │   │   ├── GeneradorPDFService
│   │   │   │   └── ...
│   │   │   └── ui/            # Interfaz gráfica
│   │   │       ├── views/
│   │   │       │   ├── carrito/CarritoViewController
│   │   │       │   ├── comparador/ComparadorController
│   │   │       │   └── constructor/ConstructorModularController
│   │   │       └── three/     # Visualización 3D (JavaFX)
│   │   └── resources/
│   │       ├── fxml/          # Layouts (carrito.fxml, etc.)
│   │       └── application.properties
│   └── test/
│       └── java/pe/innovasur/sofas/
│           ├── service/       # Tests unitarios
│           ├── ui/            # Tests de controladores
│           └── integration/   # Tests E2E (CarritoFlowE2ETest)
├── pom.xml                    # Dependencias Maven
├── GUIA_USUARIO.md            # Documentación para usuarios
└── INSTALACION_TECNICA.md     # Este archivo
```

---

## Dependencias Principales

```xml
<!-- Spring Boot -->
<spring-boot-starter-data-jpa>
<spring-boot-starter-validation>

<!-- JavaFX (Interfaz Gráfica) -->
<javafx-controls>
<javafx-fxml>
<javafx-graphics>

<!-- Base de Datos -->
<sqlite-jdbc>
<hibernate-core>

<!-- Generación de PDF -->
<openpdf>

<!-- Utilidades -->
<lombok>              <!-- Reducción de boilerplate -->
<exp4j>               <!-- Evaluación de expresiones matemáticas -->

<!-- Testing -->
<spring-boot-starter-test>
<junit-jupiter>
<mockito-core>
```

---

## Configuración

### 1. Base de Datos (SQLite)

La base de datos se crea automáticamente en:
```
~/.sistemaerpsofas/sofas.db
```

Para resetear datos:
```bash
rm ~/.sistemaerpsofas/sofas.db
./mvnw javafx:run  # Se recreará con datos de semilla
```

### 2. Application Properties

**Archivo**: `src/main/resources/application.properties`

```properties
# Spring Boot
spring.application.name=innova-sur-sofas
spring.jpa.hibernate.ddl-auto=update

# SQLite
spring.datasource.url=jdbc:sqlite:${user.home}/.innova-sur-sofas/sofas.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# JPA
spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect

# Logging
logging.level.root=INFO
logging.level.pe.erp.sofas=DEBUG
```

### 3. Configuración de JavaFX

El proyecto usa Maven Plugin para JavaFX:
```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>pe.erp.sofas.Launcher</mainClass>
    </configuration>
</plugin>
```

---

## Servicios Clave

### 1. GestorCarritoService
```java
obtenerOCrearCarrito(Usuario)      // Carrito por usuario
agregarAlCarrito(Usuario, ID, qty)  // Agregar item
actualizarCantidad(Usuario, ID, qty) // Modificar qty
vaciarCarrito(Usuario)              // Limpiar carrito
obtenerDetalleCarrito(Usuario)      // Resumen con totales
```

### 2. CalculoPrecioService
```java
calcularPrecio(ConfigSofa)          // Precio dinámico
calcularCostoBOM(ConfigSofa)        // Costo materiales
calcularCostoMO(ConfigSofa)         // Costo mano de obra
calcularConMargenYDescuento(...)    // Con descuentos
```

### 3. GeneradorPDFService
```java
generarPresupuesto(ConfigSofa, cliente) // PDF con especificaciones
```

### 4. CreadorPedidoService
```java
crearPedidosDesdeCarrito(Usuario, Cliente, vaciar)
generarCodigoPedido()               // PED-YYYYMM-XXXXX
```

### 5. PersistenciaConfiguracionService
```java
guardar(Usuario, ConfigSofa, nombre, descripcion)
cargar(ID, Usuario)
listarPorUsuario(Usuario)
listarFavoritos(Usuario)
```

---

## Tests

### Ejecutar todos los tests
```bash
./mvnw test
```

### Ejecutar tests de un paquete
```bash
./mvnw test -Dtest=CarritoFlowE2ETest
./mvnw test -Dtest=CalculoPrecioServiceTest
```

### Cobertura de código
```bash
./mvnw jacoco:report
# Reporte en: target/site/jacoco/index.html
```

---

## Problemas Comunes

### ❌ Error: "JAVA_HOME not found"
```bash
# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

### ❌ Error: "JavaFX modules not found"
```bash
# Limpiar caché Maven
rm -rf ~/.m2/repository/org/openjfx

# Recompile
./mvnw clean compile
```

### ❌ SQLite: "Database is locked"
- Cierra todas las instancias de la aplicación
- Elimina `sofas.db-journal` si existe
- Reinicia la app

### ❌ Tests fallan en headless mode
```bash
./mvnw test -Dheadless=false
```

---

## Build & Deployment

### 1. Compilar JAR Ejecutable
```bash
./mvnw clean package -DskipTests
```

Genera: `target/sistema-erp-sofas-1.0.0.jar`

### 2. Crear Instalador (Windows/macOS)
```bash
./mvnw clean javafx:jlink
./mvnw jpackage:jpackage
```

Genera:
- Windows: `.exe` installer
- macOS: `.dmg` installer
- Linux: `.deb` package

### 3. Distribuir

```bash
# Copiar JAR
cp target/sistema-erp-sofas-1.0.0.jar /ruta/distribucion/

# Copiar archivos de configuración
cp src/main/resources/application.properties /ruta/distribucion/

# Copiar guía de usuario
cp GUIA_USUARIO.md /ruta/distribucion/
```

---

## Monitoreo en Producción

### Logs
```bash
tail -f ~/.sistemaerpsofas/sistema-erp-sofas.log
```

### Base de Datos
```bash
# Conectar a SQLite
sqlite3 ~/.sistemaerpsofas/sofas.db

# Ver tablas
.tables

# Consulta de ejemplo
SELECT COUNT(*) FROM pedido;
```

### Métricas
```bash
# Memoria
ps aux | grep java

# CPU
top | grep java
```

---

## Actualización a Nuevas Versiones

```bash
# 1. Backup de base de datos
cp ~/.sistemaerpsofas/sofas.db ~/.sistemaerpsofas/sofas.db.backup

# 2. Git pull de nuevo código
git pull origin main

# 3. Compilar
./mvnw clean package

# 4. Ejecutar
./mvnw javafx:run

# 5. Si problemas, restaurar
cp ~/.sistemaerpsofas/sofas.db.backup ~/.sistemaerpsofas/sofas.db
```

---

## Contribuir al Proyecto

### 1. Fork el repositorio
```bash
git clone https://github.com/TU_USUARIO/Sistema-de-Muebles-Exclusivo-Sofas.git
```

### 2. Crea rama para tu feature
```bash
git checkout -b feature/mi-feature
```

### 3. Commit con mensaje claro
```bash
git commit -m "Agregar nueva funcionalidad X"
```

### 4. Push y Pull Request
```bash
git push origin feature/mi-feature
# Abre Pull Request en GitHub
```

---

## Soporte Técnico

- 📧 **Email Dev**: dev@innovasur.pe
- 🐛 **Issues**: github.com/.../issues
- 📖 **Wiki**: github.com/.../wiki
- 💬 **Discussions**: github.com/.../discussions

---

## Licencia

Este proyecto está bajo licencia **propietaria de SISTEMA ERP SOFAS**.

Todos los derechos reservados © 2026 SISTEMA ERP SOFAS.

---

**Versión**: 1.0  
**Actualizado**: Octubre 2026  
**Mantenedor**: Pedro Delgado (@pedrodelgado-dev)
