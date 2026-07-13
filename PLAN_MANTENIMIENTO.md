# Plan de Mantenimiento — El Paisa Digital

## 1. Scripts de mantenimiento incluidos

Ubicados en la carpeta `scripts/`:

| Script | Qué hace |
|---|---|
| `backup_db.bat` | Genera un dump `.sql` completo de la base de datos con fecha y hora en el nombre, y conserva automáticamente solo los **últimos 10 backups** (borra los más antiguos para no llenar el disco) |
| `restore_db.bat` | Restaura la base de datos desde un archivo de backup específico, con confirmación explícita antes de sobrescribir datos |
| `credenciales_db.bat.ejemplo` | Plantilla de configuración — **cada integrante del equipo debe copiarla como `credenciales_db.bat`** (sin `.ejemplo`) y poner ahí su usuario/contraseña real de MySQL local |

### ⚠️ Por qué las credenciales NO están en el script directamente

Como el repositorio es público en GitHub, poner la contraseña real de la base de datos directamente en `backup_db.bat` la expondría a cualquiera. Por eso:
1. Los scripts leen las credenciales desde `scripts/credenciales_db.bat`
2. Ese archivo específico está en `.gitignore` — **nunca se sube a GitHub**
3. Solo se sube la plantilla `credenciales_db.bat.ejemplo`, sin datos reales

### Preparación (una sola vez, por cada integrante)

```cmd
cd scripts
copy credenciales_db.bat.ejemplo credenciales_db.bat
notepad credenciales_db.bat
```
Y completar ahí `DB_USER` y `DB_PASSWORD` con los datos reales de su MySQL local.

### Uso manual

```cmd
scripts\backup_db.bat
```
Genera el archivo en `backups\elpaisadb_<fecha>_<hora>.sql`.

```cmd
scripts\restore_db.bat elpaisadb_11-07-2026_1930.sql
```

## 2. Backup automático programado ("cron job" en Windows)

Windows no usa `cron` (eso es de Linux/Mac), su equivalente es el **Programador de Tareas** (Task Scheduler). Se puede programar el backup diario por línea de comandos:

```cmd
schtasks /create /tn "Backup El Paisa Digital" /tr "C:\ruta\completa\al\proyecto\scripts\backup_db.bat" /sc daily /st 23:30
```

Esto crea una tarea llamada "Backup El Paisa Digital" que ejecuta el script **todos los días a las 11:30 PM**, sin necesidad de que nadie lo recuerde manualmente.

Para verificar que quedó programada:
```cmd
schtasks /query /tn "Backup El Paisa Digital"
```

Para eliminarla si ya no se necesita:
```cmd
schtasks /delete /tn "Backup El Paisa Digital" /f
```

(En un servidor Linux real, el equivalente sería agregar una línea a `crontab -e`: `30 23 * * * /ruta/backup_db.sh`)

## 3. Rotación y limpieza de logs

Ya configurada en `logback-spring.xml` — **no requiere intervención manual**:
- Cada día se crea un archivo de log nuevo (`elpaisadigital.log`)
- Se conservan automáticamente los últimos **15 días** de logs; los más antiguos se eliminan solos

## 4. Calendario de mantenimiento

| Actividad | Frecuencia | Responsable |
|---|---|---|
| Backup de la base de datos | Diario (automático vía Task Scheduler) | Automatizado |
| Verificar que el último backup se generó correctamente | Semanal | Encargado de control de versiones |
| Probar una restauración completa en un entorno de prueba | Mensual | Administrador del sistema |
| Revisar logs en busca de errores recurrentes | Semanal | Encargado técnico |
| Revisar y actualizar dependencias del `pom.xml` (versiones de Spring Boot, librerías) | Trimestral | Equipo de desarrollo |
| Verificar espacio en disco de la carpeta `backups/` | Mensual | Administrador del sistema |

## 5. Procedimiento ante una falla crítica (recuperación)

1. Detener la aplicación
2. Identificar el backup más reciente y confiable en `backups/`
3. Ejecutar `scripts\restore_db.bat <archivo>`
4. Verificar con `/actuator/health` que la app vuelve a reportar `UP`
5. Documentar el incidente (qué pasó, cuándo, cómo se resolvió) para prevenirlo a futuro
