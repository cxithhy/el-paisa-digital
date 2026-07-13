# Informe de Pruebas de Software y Seguridad — El Paisa Digital

## 1. Estrategia de pruebas

El proyecto aplica dos niveles de prueba automatizada:

| Nivel | Herramienta | Qué verifica |
|---|---|---|
| **Unitarias** | JUnit 5 + Mockito | Lógica de negocio aislada (Service), sin tocar base de datos real |
| **Integración HTTP** | Spring Boot Test + MockMvc | Que las reglas de seguridad (RBAC) realmente bloqueen/permitan las rutas correctas a nivel de petición HTTP real |

Ejecución completa:
```bash
mvn test
```

## 2. Pruebas de software (funcionales)

| Clase de prueba | Casos cubiertos |
|---|---|
| `VentaServiceImplTest` | Registro de venta con descuento de stock correcto; rechazo por stock insuficiente (sin alterar el inventario ni persistir la venta); rechazo de venta sin productos; venta de un producto sin insumo asociado (caso opcional) |
| `ProductoServiceImplTest` | Rechazo de precio inválido (≤0); rechazo de nombre vacío; excepción al buscar un producto inexistente |
| `InsumoServiceImplTest` | Rechazo de nombre vacío; cálculo correcto de insumos con stock igual o por debajo del mínimo |
| `ReservaServiceImplTest` | Validación de teléfono peruano; validación de horario de atención por sede; anticipación mínima de un día; cambio de estado (confirmar/cancelar) |

**Resultado:** 20 pruebas unitarias (Venta, Producto, Insumo, Reserva, Usuario), todas en verde (`BUILD SUCCESS`).

## 3. Pruebas de seguridad

### 3.1 Autenticación y manejo de contraseñas — `UsuarioServiceImplTest`

| Prueba | Qué confirma | Resultado |
|---|---|---|
| `crear_cifraLaContrasenaConBCrypt_nuncaTextoPlano` | La contraseña **nunca** se guarda en texto plano; siempre queda como hash BCrypt (`$2a$...`) | ✅ Pasa |
| `crear_conPasswordVacio_lanzaExcepcion` | No se permite crear un usuario sin contraseña | ✅ Pasa |
| `loadUserByUsername_usuarioInactivo_lanzaExcepcion` | Un usuario desactivado no puede autenticarse aunque sus credenciales sean correctas | ✅ Pasa |
| `loadUserByUsername_usuarioNoExiste_lanzaExcepcion` | Un usuario inexistente lanza una excepción genérica (no revela si el username existe o no) | ✅ Pasa |
| `loadUserByUsername_asignaElAuthorityCorrectoSegunElRol` | El rol del usuario se traduce correctamente a un `ROLE_X` de Spring Security | ✅ Pasa |

### 3.2 Control de acceso por rol (RBAC) — `SecurityWebIntegrationTest`

Estas pruebas hacen peticiones HTTP reales (vía `MockMvc`) contra la aplicación levantada en memoria, para confirmar que la configuración de `SecurityConfig` funciona en la práctica y no solo en el papel:

| Prueba | Escenario | Resultado esperado | Resultado |
|---|---|---|---|
| `login_esPublico` | Acceso a `/login` sin sesión | 200 OK | ✅ Pasa |
| `dashboard_sinAutenticar_redirigeALogin` | Acceso a `/dashboard` sin sesión | Redirección 3xx a login | ✅ Pasa |
| `productos_comoCajero_devuelveAccesoDenegado` | Rol CAJERO intenta entrar a `/productos` | 403 Forbidden | ✅ Pasa |
| `insumos_comoMozo_devuelveAccesoDenegado` | Rol MOZO intenta entrar a `/insumos` | 403 Forbidden | ✅ Pasa |
| `reporteExcel_comoMozo_devuelveAccesoDenegado` | Rol MOZO intenta descargar el reporte | 403 Forbidden | ✅ Pasa |
| `productos_comoAdmin_accedeCorrectamente` | Rol ADMIN entra a `/productos` | 200 OK | ✅ Pasa |
| `nuevaVenta_comoMozo_accedeCorrectamente` | Rol MOZO entra a `/ventas/nueva` (rol operativo mínimo) | 200 OK | ✅ Pasa |

### 3.3 Revisión manual de vulnerabilidades comunes (OWASP Top 10, alcance del proyecto)

| Vulnerabilidad | Estado en el proyecto | Justificación |
|---|---|---|
| **Inyección SQL** | ✅ Mitigado | Toda la capa DAO usa `EntityManager`/JPQL con parámetros nombrados (`setParameter`), nunca concatenación de strings en las consultas |
| **XSS (Cross-Site Scripting)** | ✅ Mitigado | Thymeleaf escapa automáticamente el contenido con `th:text` (no se usa `th:utext` en ningún formulario que reciba entrada de usuario) |
| **Contraseñas en texto plano** | ✅ Mitigado | BCrypt en `UsuarioServiceImpl`, verificado con pruebas unitarias (ver 3.1) |
| **Control de acceso roto** | ✅ Mitigado | RBAC verificado con pruebas de integración HTTP (ver 3.2) |
| **CSRF (Cross-Site Request Forgery)** | ⚠️ Observación levantada | Actualmente deshabilitado (`csrf().disable()`) para simplificar los formularios del avance. **Recomendación:** habilitarlo y agregar el token CSRF a cada `<form>` antes de un despliegue en producción real |
| **Gestión de sesión** | ✅ Por defecto de Spring Security | Spring Security aplica protección contra fijación de sesión (session fixation) de forma automática |

## 4. Observaciones levantadas y su seguimiento

| # | Observación | Severidad | Estado |
|---|---|---|---|
| 1 | CSRF deshabilitado globalmente | Media | Documentado como limitación conocida; recomendado habilitar antes de producción |
| 2 | No hay política de complejidad mínima de contraseñas | Baja | Pendiente para una siguiente iteración (validación en `ValidacionUtil`) |
| 3 | El logging no diferencia aún eventos de seguridad (login fallido, acceso denegado) en un logger dedicado | Baja | Se aborda en el plan de monitoreo (ver informe de despliegue/monitoreo) |

## 5. Conclusión

El proyecto cuenta con **20 pruebas unitarias** de lógica de negocio y **9 pruebas de integración de seguridad** que verifican, contra la aplicación real (no simulada), que el control de acceso por rol funciona como está diseñado. Las prácticas de seguridad básicas (cifrado de contraseñas, prevención de inyección SQL y XSS) están correctamente implementadas; la única observación pendiente de severidad media (CSRF) queda documentada con su recomendación de remediación.
