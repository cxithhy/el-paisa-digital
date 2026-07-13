# Plan de Despliegue y Monitoreo — El Paisa Digital

## 1. Despliegue

### 1.1 Empaquetado con Maven

El proyecto se despliega como un **jar ejecutable autocontenido** (incluye el servidor Tomcat embebido de Spring Boot, no requiere instalar un servidor de aplicaciones aparte):

```bash
mvn clean package
```

Esto genera `target/elpaisadigital-1.0.0.jar`. Maven ejecuta automáticamente las pruebas antes de empaquetar (`mvn test` como parte del ciclo de vida `package`) — si alguna prueba falla, el jar no se genera, lo que evita desplegar código roto.

### 1.2 Ejecución

```bash
java -jar target/elpaisadigital-1.0.0.jar
```

Por defecto usa el perfil `mysql` (definido en `application.properties`), así que la base de datos `elpaisadb` debe existir y estar accesible antes de arrancar. Para otro entorno (ej. una demo rápida sin MySQL):

```bash
java -jar target/elpaisadigital-1.0.0.jar --spring.profiles.active=h2
```

### 1.3 Configuración de "servidor" para este avance

Para este avance el despliegue se demuestra de forma **local** (jar ejecutable corriendo en la máquina del equipo), que es válido según la consigna ("puedes realizarla individualmente o en grupo" con flexibilidad de alcance). El proyecto ya está preparado para un despliegue real en un servidor remoto sin cambios de código, solo de configuración:

| Aspecto | Configuración lista para producción |
|---|---|
| Puerto | Configurable vía `server.port` o variable de entorno `SERVER_PORT` |
| Base de datos | Perfil `mysql` con URL/usuario/password externalizables como variables de entorno (`SPRING_DATASOURCE_URL`, etc.) |
| Proceso en segundo plano | El jar puede registrarse como servicio `systemd` (Linux) o ejecutarse con `nohup java -jar app.jar &` |
| Proxy reverso | Recomendado Nginx delante del puerto 8080 para HTTPS (certificado TLS) y balanceo si se escala a más de una instancia |

## 2. Monitoreo

### 2.1 Health checks (Spring Boot Actuator)

Se integró `spring-boot-starter-actuator`, el estándar de facto en el ecosistema Spring para exponer el estado de salud de la aplicación:

| Endpoint | Acceso | Qué muestra |
|---|---|---|
| `GET /actuator/health` | Público | Estado general (`UP`/`DOWN`) de la app, la base de datos y el disco |
| `GET /actuator/info` | Público | Nombre, descripción y versión de la aplicación |
| `GET /actuator/metrics` | Solo ADMIN | Métricas de la JVM (memoria, threads, GC), del pool de conexiones HikariCP, y de peticiones HTTP |
| `GET /actuator/loggers` | Solo ADMIN | Permite ver y cambiar el nivel de log de cualquier paquete **en caliente**, sin reiniciar la app (útil para depurar un problema en producción sin downtime) |

`/actuator/health` se dejó público a propósito: es la convención estándar para que un balanceador de carga o un servicio externo de monitoreo (ej. UptimeRobot, un healthcheck de Docker/Kubernetes) pueda verificar que la app sigue viva sin necesitar autenticarse.

### 2.2 Logs (Logback)

Ya configurado en `logback-spring.xml`:
- Salida a consola **y** a archivo rotado diariamente (`logs/elpaisadigital.log`, se conservan 15 días de historial)
- El paquete `com.elpaisa` loguea en nivel `INFO`, registrando cada venta, creación de usuario, y error de negocio (stock insuficiente, recurso no encontrado)

**Qué se debe revisar en los logs regularmente:**
- Errores `ERROR` repetidos del mismo tipo → puede indicar un bug o un ataque (ej. muchos intentos de acceso denegado)
- Ventas rechazadas por `StockInsuficienteException` → puede indicar que hay que reabastecer o ajustar el stock mínimo de un insumo

### 2.3 Plan de monitoreo (frecuencia y responsables)

| Actividad | Frecuencia | Responsable | Herramienta |
|---|---|---|---|
| Verificar `/actuator/health` | Diaria (o automatizada cada 5 min con un cron/uptime checker) | Encargado técnico del turno | curl / navegador / servicio externo |
| Revisar `logs/elpaisadigital.log` en busca de errores | Diaria | Encargado técnico | Lectura directa o `grep ERROR` |
| Revisar métricas de memoria/CPU (`/actuator/metrics`) | Semanal | Administrador del sistema | Actuator + `jconsole`/`VisualVM` si se requiere detalle |
| Revisar alertas de stock bajo (`/insumos`, tarjeta de stock bajo) | Diaria | Encargado de cocina/compras | Interfaz web del sistema |

## 3. Cómo verificar el monitoreo en la sustentación

1. Levantar la app: `mvn spring-boot:run`
2. Abrir en el navegador: `http://localhost:8080/actuator/health` → debe mostrar `{"status":"UP"}`
3. Iniciar sesión como `admin` y abrir `http://localhost:8080/actuator/metrics` → mostrar alguna métrica (ej. `jvm.memory.used`)
4. Mostrar el archivo `logs/elpaisadigital.log` generándose en tiempo real mientras se registra una venta desde la interfaz
