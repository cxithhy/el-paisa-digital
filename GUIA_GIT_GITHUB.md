# Guía de Control de Versiones — El Paisa Digital

Cumple con el criterio de la rúbrica: *"Integrar el proyecto a un sistema de control de versiones utilizando Git y GitHub, con evidencia del 100% de los avances."*

## 1. Responsable de control de versiones

Designen a **una persona del equipo** como responsable de:
- Revisar que cada Pull Request tenga una descripción clara.
- Verificar que no se suban archivos innecesarios (`target/`, `.idea/`, etc. — ya están en `.gitignore`).
- Mantener la rama `main` siempre estable (que compile y corra).

## 2. Primeros pasos (una sola vez)

```bash
cd elpaisadigital
git init
git add .
git commit -m "feat: arquitectura inicial MVC+DAO+SOLID, login y registro de ventas con descuento de stock"
```

En GitHub: crear un repositorio nuevo (por ejemplo `el-paisa-digital`), luego:

```bash
git branch -M main
git remote add origin https://github.com/<usuario-o-equipo>/el-paisa-digital.git
git push -u origin main
```

## 3. Flujo de trabajo recomendado (uno por integrante)

Cada integrante trabaja en su propia rama y luego la integra a `main`:

```bash
git checkout -b feature/reportes-excel
# ... hacen cambios ...
git add .
git commit -m "feat: agrega generacion de reporte de ventas en Excel con Apache POI"
git push origin feature/reportes-excel
```

Luego se abre un **Pull Request** en GitHub hacia `main` para que el equipo revise antes de fusionar.

## 4. Convención de mensajes de commit

Usar prefijos claros para que el historial sea legible (y se note el avance real, no solo un commit gigante al final):

| Prefijo | Cuándo usarlo |
|---|---|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de un error |
| `docs:` | Cambios en documentación (README, informe) |
| `test:` | Pruebas unitarias nuevas o corregidas |
| `refactor:` | Cambios internos sin alterar el comportamiento |

Ejemplos reales de este avance:
```
feat: implementa capa DAO generica con EntityManager
feat: agrega VentaService con descuento automatico de stock por receta
test: agrega pruebas unitarias TDD para registro de ventas
feat: integra Spring Security con roles y BCrypt
feat: agrega reporte de ventas en Excel con Apache POI
docs: actualiza README con arquitectura y librerias usadas
```

## 5. Evidencia para la entrega

Antes de sustentar, verifiquen en GitHub que:
- [ ] El repositorio es visible para el docente (público o con el docente invitado como colaborador).
- [ ] Hay **varios commits** repartidos en el tiempo (no uno solo de última hora).
- [ ] Cada integrante tiene al menos un commit propio (para que se note la participación de todos).
- [ ] El `README.md` está actualizado y explica cómo correr el proyecto.

## 6. Taller de versionamiento

Recuerden que la consigna pide **participar en el taller de versionamiento** del curso — asistir y completar los ejercicios prácticos que indique el docente, además de aplicar lo aprendido aquí.
