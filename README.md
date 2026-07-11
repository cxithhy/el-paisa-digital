# El Paisa Digital — Avance de Proyecto Final 3

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

## 3. Funcionalidad cubierta en este avance (~35-40%)

- ✅ RF01: Registro de pedidos digitales vinculados a una sede.
- ✅ RF02: Descuento automático de inventario según el insumo principal asociado a cada plato (relación opcional producto→insumo, sin tabla intermedia).
- ✅ RF04: Reporte de ventas exportable a Excel.
- ✅ RNF01: Seguridad por roles y contraseñas cifradas.
- 🔜 RF03 (reservas online), RF05 (asistencia de personal): planificados para el Avance Final.

## 4. Cómo ejecutar el proyecto

Requisitos: JDK 17+, Maven 3.8+.

```bash
mvn spring-boot:run
```

La aplicación levanta con una base de datos **H2 en memoria** (no requiere instalar MySQL para la demo) y se pre-cargan datos de ejemplo al iniciar (ver `DataInitializer.java`).

- URL: http://localhost:8080/login
- Usuario admin: `admin` / `admin123`
- Usuario cajero: `cajero1` / `cajero123`

Para producción con MySQL, editar `src/main/resources/application.properties` (las líneas ya están comentadas y listas para descomentar).

## 5. Control de versiones (Git y GitHub)

Ver `GUIA_GIT_GITHUB.md` para el paso a paso de cómo subir este repositorio a GitHub y la convención de commits usada por el equipo.

## 6. Pruebas unitarias (TDD)

```bash
mvn test
```

`VentaServiceImplTest` cubre:
1. Venta exitosa con descuento correcto de stock.
2. Venta rechazada por stock insuficiente (no debe alterar el inventario ni guardar la venta — atomicidad).
3. Venta rechazada por no tener productos en el detalle.

`ProductoServiceImplTest` cubre validaciones de precio y nombre.

## 7. Referencias

- Fowler, M. (2002). *Patterns of Enterprise Application Architecture*. Addison-Wesley.
- Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
- Spring Team. (s. f.). *Spring Boot Reference Documentation*. https://docs.spring.io/spring-boot/
- Apache Software Foundation. (s. f.). *Apache POI - the Java API for Microsoft Documents*. https://poi.apache.org/
- Google. (s. f.). *Guava: Google Core Libraries for Java*. https://github.com/google/guava
- QOS.ch. (s. f.). *Logback Project*. https://logback.qos.ch/
- Git. (s. f.). *Pro Git Book*. https://git-scm.com/book
