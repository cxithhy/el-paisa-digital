# Anexos técnicos — Diagramas y documentación de código

Esta carpeta contiene el material que pide la sección **6. Anexos** de la consigna.

## 1. Diagramas de arquitectura (`diagramas/`)

| Archivo | Qué muestra |
|---|---|
| `01_arquitectura_capas.png/.svg` | Arquitectura en capas: Controller → Service → DAO → Model, más la capa transversal de seguridad (Spring Security/RBAC) y la vista Thymeleaf. |
| `02_patron_dao_solid.png/.svg` | Patrón DAO genérico y cómo se aplican OCP, LSP e ISP: `GenericDao<T,ID>` extendido por interfaces específicas, implementado por `GenericDaoImpl` y sus subclases. |
| `03_modelo_datos_er.png/.svg` | Modelo entidad-relación: Usuario, Rol, Sede, Producto, Insumo, Venta, DetalleVenta, Reserva, Asistencia y sus relaciones. |

Generados con Graphviz a partir de la estructura real del código (paquetes `model/`, `dao/`, `service/`). Insertar las versiones `.png` directamente en el informe de Word; usar los `.svg` si se necesita escalar sin perder calidad.

## 2. Documentación de código (`documentacion-tecnica/index.html`)

Resumen técnico de las 57 clases del proyecto, extraído automáticamente de los comentarios Javadoc que ya existen en el código fuente (paquete, descripción de clase y de los métodos documentados). Ábrelo en cualquier navegador.

**Esto es un respaldo rápido, no reemplaza el Javadoc oficial.** Para generar la documentación HTML completa y con el formato estándar de Javadoc (todas las clases, jerarquías, enlaces cruzados entre tipos), en una máquina con el **JDK completo** (no solo el JRE) instalado, ejecutar desde la raíz del proyecto:

```bash
mvn javadoc:javadoc
```

El resultado queda en `target/site/apidocs/index.html`. El plugin (`maven-javadoc-plugin`) ya está configurado en el `pom.xml`, así que no requiere ningún paso adicional aparte de tener el JDK instalado.
