# 🚀 LABORATORIO ESPECIAL — CI/CD para Aplicaciones Java + Spring Boot

**Módulo 6.1 · Laboratorio Especial · Proyecto LibroTech 📚**  
**Duración estimada:** 6–8 horas (dividido en 5 fases progresivas)  
**Nivel:** Desde principiante hasta avanzado  

---

## 📋 ¿De qué trata este laboratorio?

Este laboratorio te guiará paso a paso para implementar un **pipeline de Integración Continua y Entrega Continua (CI/CD)** en tu proyecto **LibroTech**. Aprenderás a automatizar todo el ciclo de vida de tu aplicación: desde que haces un `git push` hasta que el proyecto se compila, se prueban los tests, se empaqueta y queda listo para desplegarse.

Lo mejor: **se puede (y se debe) empezar desde el primer día**. No necesitas tener tests ni funcionalidades avanzadas — el pipeline crece junto con tu proyecto.

---

## 🎯 ¿Por qué CI/CD desde el día 1?

### La realidad en la industria

En empresas como Google, Netflix, Amazon o MercadoLibre, **ningún código llega a producción sin pasar por un pipeline automatizado**. No importa si es un cambio de una línea o de mil líneas — el proceso es siempre el mismo:

```
Developer → Push → Pipeline CI → Tests → Build → Deploy
```

### ¿Qué pasa sin CI/CD?

Imagina un equipo de 5 desarrolladores trabajando en LibroTech SIN CI/CD:

```
Lunes    → Ana sube su código. Funciona en su máquina. ✅
Martes   → Carlos sube su código. También funciona en su máquina. ✅
Miércoles → Intentan juntar todo. NADA FUNCIONA. ❌❌❌
           Pasan 2 días arreglando conflictos.
```

### ¿Qué pasa CON CI/CD?

```
Lunes    → Ana hace push. El pipeline compila y corre tests. Todo verde. ✅
Martes   → Carlos hace push. El pipeline detecta que su cambio rompe
           un test de Ana. Carlos lo corrige en 10 minutos. ✅
Miércoles → Todo funciona perfectamente. El equipo avanza rápido. 🚀
```

---

## 🧠 Conceptos Clave (explicados simple)

### CI — Integración Continua (Continuous Integration)

**¿Qué es?** Cada vez que un desarrollador sube código al repositorio (push/merge), un servidor automáticamente:
1. Descarga el código
2. Lo compila
3. Ejecuta las pruebas automatizadas
4. Reporta si algo falló

**Analogía:** Es como tener un inspector de calidad que revisa cada pieza que sale de la fábrica. No deja pasar piezas defectuosas.

### CD — Entrega Continua (Continuous Delivery)

**¿Qué es?** Si el paso de CI fue exitoso (todo compiló, todos los tests pasaron), el sistema automáticamente:
1. Empaqueta la aplicación (genera el `.jar`)
2. La despliega en un ambiente de staging o producción

**Analogía:** Una vez que el inspector aprueba la pieza, la cinta transportadora la lleva automáticamente al camión de envío. No necesitas que alguien la lleve caminando.

### Pipeline

**¿Qué es?** La secuencia completa de pasos automatizados. Es como una "receta" que le dices al servidor: "Cuando alguien haga push, ejecuta estos pasos en este orden."

```
Pipeline de LibroTech:

┌─────────┐   ┌──────────┐   ┌─────────┐   ┌──────────┐   ┌──────────┐
│ Checkout│──►│ Compile  │──►│  Test   │──►│  Build   │──►│ Artifact │
│  📥     │   │  🔨      │   │  🧪     │   │  📦      │   │  📤      │
└─────────┘   └──────────┘   └─────────┘   └──────────┘   └──────────┘
   Git          mvn compile    mvn test     mvn package     .jar listo
```

Si algún paso falla, el pipeline se **detiene** y alerta al desarrollador.

---

## 📚 ¿Qué necesitas ANTES de empezar?

| Requisito | ¿Lo tengo? |
|---|---|
| Cuenta en **GitHub** (gratuita) | ☐ |
| **Git** instalado en tu computadora | ☐ |
| Proyecto **LibroTech** en Spring Boot (puede ser desde la Semana 1) | ☐ |
| Conocimientos básicos de Git (commit, push, pull) | ☐ |

> 💡 **No necesitas tener tests implementados** para empezar. La Fase 1 funciona con un proyecto que solo compila. Los tests se van agregando en fases posteriores.

---

## 🗺️ Hoja de Ruta — Las 5 Fases

| Fase | Archivo | ¿Qué aprenderás? | ¿Desde cuándo aplica? | Duración |
|---|---|---|---|---|
| **Fase 1** | `01-Fase1-GitHub-Actions-Primer-Pipeline.md` | Crear tu primer pipeline CI con GitHub Actions. Compilar automáticamente con cada push. | **Semana 1 en adelante** | ~1.5 h |
| **Fase 2** | `02-Fase2-Pipeline-Testing-Reportes.md` | Agregar ejecución de tests y reportes de cobertura al pipeline. Badges de estado. | **Semana 2 en adelante** (cuando tengas tests) | ~1.5 h |
| **Fase 3** | `03-Fase3-Ambientes-Secretos-Variables.md` | Manejar configuraciones por ambiente (dev, test, prod). Secretos y variables de entorno. | **Semana 2 en adelante** | ~1 h |
| **Fase 4** | `04-Fase4-Docker-Containerizacion.md` | Empaquetar tu app Spring Boot en un contenedor Docker. Crear y publicar imágenes. | **Semana 3 en adelante** | ~2 h |
| **Fase 5** | `05-Fase5-Pipeline-Completo-Profesional.md` | Pipeline profesional con múltiples stages, protección de ramas, y deploy automatizado. | **Semana 4 (proyecto maduro)** | ~1.5 h |

---

## 🔧 ¿Qué herramienta de CI/CD usaremos?

Usaremos **GitHub Actions** porque:

| Ventaja | Detalle |
|---|---|
| **Gratuita** | 2,000 minutos/mes en el plan gratuito (más que suficiente) |
| **Integrada** | Vive dentro de GitHub, donde ya tienes tu repositorio |
| **Estándar de la industria** | Millones de proyectos la usan. Aprenderla te da una ventaja laboral real |
| **Sin infraestructura** | No necesitas instalar servidores. GitHub provee las máquinas |
| **YAML simple** | Los pipelines se definen en archivos `.yml` fáciles de leer |

### ¿Y Jenkins? ¿Y GitLab CI?

Son excelentes herramientas, pero:
- **Jenkins** requiere instalar y mantener un servidor propio.
- **GitLab CI** requiere que tu repositorio esté en GitLab.

Los **conceptos** que aprendas aquí son **transferibles** a cualquier herramienta de CI/CD. El 90% de lo que aprendas con GitHub Actions aplica directamente en Jenkins, GitLab CI, CircleCI, etc.

---

## 📂 ¿Dónde va la configuración de CI/CD en tu proyecto?

GitHub Actions busca los archivos de pipeline en esta ruta específica:

```
tu-proyecto-librotech/
├── .github/
│   └── workflows/          ← 🎯 AQUÍ van los pipelines
│       ├── ci.yml          ← Pipeline principal (Fase 1)
│       ├── test-report.yml ← Pipeline de reportes (Fase 2)
│       └── deploy.yml      ← Pipeline de deploy (Fase 5)
├── src/
│   ├── main/
│   └── test/
├── Dockerfile              ← Contenedor (Fase 4)
├── pom.xml
└── ...
```

> ⚠️ **La ruta `.github/workflows/` es obligatoria.** Si la escribes mal (por ejemplo, `.github/workflow/` sin la "s"), GitHub no detectará tus pipelines.

---

## 🚀 ¿Listo? Comienza con la Fase 1

Abre el archivo **`01-Fase1-GitHub-Actions-Primer-Pipeline.md`** y configura tu primer pipeline. Solo necesitas un proyecto Spring Boot que compile — no necesitas tests ni funcionalidades avanzadas.

> 🧠 **Consejo**: Lo ideal es hacer esta Fase 1 **al mismo tiempo** que el Lab 1 de la Semana 1 (cuando creas tu primer Controller). Así desde tu primer commit, el pipeline ya está funcionando.
