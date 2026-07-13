# El Paisa Digital — Proyecto Final

Sistema de Gestión y Plataforma Web Integral — Curso Integrador I: Sistemas Software

## 1. Arquitectura aplicada

| Principio | Cómo se aplica en el proyecto |
|---|---|
| **MVC** | `controller/` (Model-View-Controller vía Spring MVC) renderiza vistas Thymeleaf en `templates/`; `model/` contiene las entidades JPA. |
| **DAO** | Capa `dao/` con interfaces + implementaciones (`dao/impl/`) que usan `EntityManager` directamente, separando el acceso a datos de la lógica de negocio. |
| **TDD** | `src/test/java/.../service/` contiene pruebas JUnit 5 + Mockito escritas para la regla de negocio crítica (descuento de stock) antes de confiar en la implementación final. |
| **SOLID** | Ver detalle abajo. |

### SOLID en detalle
- **SRP**: cada clase tiene una única razón de cambio (`ValidacionUtil` solo valida, `ReporteServiceImpl` solo genera reportes, cada DAO solo persiste su entidad).
- **OCP**: `GenericDaoImpl<T, ID>` se extiende (`ProductoDaoImpl`, `InsumoDaoImpl`, etc.) sin modificar la clase base.
- **LSP**: cualquier implementación de `GenericDao` puede sustituir a la interfaz sin romper el contrato.
- **ISP**: interfaces pequeñas y específicas (`ProductoDao`, `VentaService`, etc.) en vez de una interfaz gigante.
- **DIP**: los `Service` dependen de interfaces DAO (inyectadas por constructor), no de implementaciones concretas — esto es lo que permite testear `VentaServiceImpl` con mocks, sin base de datos real.

### Seguridad (RNF01)
- Autenticación con Spring Security, contraseñas cifradas con **BCrypt**.
- Autorización por rol: `ADMIN`, `CAJERO`, `MOZO` (ver `SecurityConfig.java`).

## 2. Librerías de apoyo integradas

| Librería | Uso concreto en el proyecto |
|---|---|
| **Google Guava** | `Preconditions` en `ValidacionUtil` (validación de montos/cantidades); `ImmutableList` en `InsumoServiceImpl`. |
| **Apache POI** | `ReporteServiceImpl` genera el reporte de ventas en Excel (`.xlsx`) — cubre RF04. |
| **Apache Commons Lang3** | `StringUtils.isBlank` en `ValidacionUtil`. |
| **Logback** | Configurado en `logback-spring.xml`; logging estructurado en consola + archivo (`logs/elpaisadigital.log`), usado en toda la capa de servicios. |

## 3. Funcionalidad cubierta (100%)

- ✅ RF01: Registro de pedidos digitales vinculados a una sede.
- ✅ RF02: Descuento automático de inventario según el insumo principal asociado a cada plato (relación opcional producto→insumo, sin tabla intermedia).
- ✅ RF03: Reservas online por sede, con validación de teléfono peruano y horario de atención, y flujo de confirmación/cancelación.
- ✅ RF04: Reporte de ventas exportable a Excel.
- ✅ RF05: Registro de asistencia (entrada/salida) del personal, con vista de historial para el ADMIN.
- ✅ RNF01: Seguridad por roles y contraseñas cifradas.

## 4. Cómo ejecutar el proyecto

Requisitos: JDK 17+, Maven 3.8+.

El perfil por defecto (`application.properties`) usa **MySQL**, así que la base de datos `elpaisadb` debe existir antes de arrancar:

```bash
mvn spring-boot:run
```

Para una demo rápida **sin instalar MySQL**, usar el perfil `h2` (base de datos en memoria, no persiste entre reinicios):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

En ambos casos se pre-cargan datos de ejemplo al iniciar (ver `DataInitializer.java`).

- URL: http://localhost:8080/login
- Usuario admin: `admin` / `admin123`
- Usuario cajero: `cajero1` / `cajero123`

## 5. Control de versiones (Git y GitHub)

El repositorio usa Git con commits descriptivos por funcionalidad (`feat:`, `fix:`, `test:`, `docs:`), visibles en el historial (`git log`).

## 6. Pruebas unitarias (TDD)

```bash
mvn test
```

`VentaServiceImplTest` cubre:
1. Venta exitosa con descuento correcto de stock.
2. Venta rechazada por stock insuficiente (no debe alterar el inventario ni guardar la venta — atomicidad).
3. Venta rechazada por no tener productos en el detalle.
4. Venta de un producto sin insumo asociado (relación opcional).

`ProductoServiceImplTest` e `InsumoServiceImplTest` cubren validaciones de precio, nombre y cálculo de stock bajo.

`ReservaServiceImplTest` cubre la validación de teléfono peruano, horario de atención y anticipación mínima de la reserva.

`UsuarioServiceImplTest` cubre el cifrado BCrypt de contraseñas y las reglas de autenticación (ver detalle en `INFORME_PRUEBAS.md`).

## 7. Referencias

- Fowler, M. (2002). *Patterns of Enterprise Application Architecture*. Addison-Wesley.
- Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
- Spring Team. (s. f.). *Spring Boot Reference Documentation*. https://docs.spring.io/spring-boot/
- Apache Software Foundation. (s. f.). *Apache POI - the Java API for Microsoft Documents*. https://poi.apache.org/
- Google. (s. f.). *Guava: Google Core Libraries for Java*. https://github.com/google/guava
- QOS.ch. (s. f.). *Logback Project*. https://logback.qos.ch/
- Git. (s. f.). *Pro Git Book*. https://git-scm.com/book
