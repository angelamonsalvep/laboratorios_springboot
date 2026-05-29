# 🚀 FASE 1 — Tu Primer Pipeline CI con GitHub Actions

**Laboratorio Especial de CI/CD · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Principiante  
**Aplica desde:** Semana 1 (desde tu primer Controller)  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Crear un **repositorio en GitHub** para tu proyecto LibroTech.
2. Escribir tu **primer workflow de GitHub Actions** en YAML.
3. Hacer que tu proyecto se **compile automáticamente** con cada push.
4. Leer e interpretar los **logs del pipeline** en GitHub.
5. Entender el **ciclo de vida** de un pipeline CI básico.

---

## 🧠 ¿Qué es un Workflow de GitHub Actions?

Un **workflow** (flujo de trabajo) es un archivo `.yml` que le dice a GitHub: *"Cada vez que alguien haga push a este repositorio, ejecuta estos pasos automáticamente"*.

GitHub lee ese archivo, levanta una **máquina virtual temporal** (un computador en la nube), ejecuta los pasos definidos, y te muestra el resultado: ✅ verde (todo bien) o ❌ rojo (algo falló).

### Los 4 conceptos del workflow:

```yaml
# Concepto 1: NOMBRE — Identifica el pipeline
name: LibroTech CI

# Concepto 2: TRIGGER (disparador) — ¿Cuándo se ejecuta?
on: push  # Se ejecuta cada vez que alguien hace push

# Concepto 3: JOBS (trabajos) — ¿Qué hacer?
jobs:
  build:  # Nombre del trabajo
    # Concepto 4: RUNNER — ¿En qué máquina?
    runs-on: ubuntu-latest  # Usa una máquina Ubuntu en la nube
    steps:
      - name: Paso 1
        run: echo "Hola mundo"
```

### Analogía: La receta de cocina

| Concepto del Workflow | Equivalente en cocina |
|---|---|
| `name` | Nombre de la receta: "Pasta Carbonara" |
| `on` (trigger) | ¿Cuándo cocinar? "Cuando llegue un pedido" |
| `jobs` | Los grupos de tareas: "Preparación", "Cocción", "Emplatado" |
| `runs-on` | ¿En qué cocina? "En la cocina principal" |
| `steps` | Los pasos: "Hervir agua", "Cocinar pasta", "Mezclar salsa" |

---

## 📖 Contexto de Negocio

El CTO de **LibroTech** ha decidido que todo cambio de código debe pasar por un proceso automatizado antes de llegar a la rama principal. La primera medida es simple pero poderosa: **si el código no compila, no entra.**

---

## 📝 Actividades

### Actividad 1 — Subir tu Proyecto a GitHub

Si aún no tienes tu proyecto LibroTech en GitHub, sigue estos pasos:

**1.1 — Crear el repositorio en GitHub:**
1. Ve a [github.com](https://github.com) e inicia sesión.
2. Haz clic en **"New repository"** (botón verde).
3. Nombre del repositorio: `librotech-springboot`.
4. Déjalo como **Public** (para usar GitHub Actions gratis sin límites).
5. **NO** marques "Initialize with README" (ya tienes código local).
6. Haz clic en **"Create repository"**.

**1.2 — Conectar tu proyecto local con GitHub:**

```bash
# Entra a la carpeta de tu proyecto
cd tu-proyecto-librotech

# Inicializa Git (si no lo has hecho)
git init

# Agrega todos los archivos
git add .

# Haz tu primer commit
git commit -m "feat: proyecto inicial LibroTech con Spring Boot"

# Conecta con el repositorio remoto (usa TU URL)
git remote add origin https://github.com/TU_USUARIO/librotech-springboot.git

# Sube el código
git branch -M main
git push -u origin main
```

> 💡 **¿Ya tenías el repo en GitHub?** Perfecto, salta directamente a la Actividad 2.

---

### Actividad 2 — Crear tu Primer Pipeline

**2.1 — Crear la carpeta de workflows:**

```bash
mkdir -p .github/workflows
```

**2.2 — Crear el archivo del pipeline:**

Crea el archivo `.github/workflows/ci.yml` con este contenido:

```yaml
# ============================================
# 🚀 Pipeline CI de LibroTech
# Se ejecuta con cada push al repositorio
# ============================================
name: LibroTech CI

# ¿Cuándo se ejecuta este pipeline?
on:
  push:
    branches: [ main, develop ]     # Cuando se hace push a main o develop
  pull_request:
    branches: [ main ]              # Cuando se abre un PR hacia main

jobs:
  # ────────────────────────────────────────
  # Job 1: Compilar el proyecto
  # ────────────────────────────────────────
  build:
    name: 🔨 Compilar Proyecto
    runs-on: ubuntu-latest          # Máquina virtual Ubuntu (gratis)

    steps:
      # Paso 1: Descargar el código del repositorio
      - name: 📥 Checkout del código
        uses: actions/checkout@v4
        # Esto es como hacer "git clone" dentro de la máquina virtual

      # Paso 2: Instalar Java 17
      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
        # "temurin" es la distribución de Java de Eclipse (gratuita y popular)

      # Paso 3: Cachear las dependencias de Maven
      - name: 📦 Cachear dependencias Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-
        # El caché guarda las dependencias descargadas para que la
        # próxima vez no tenga que descargarlas de nuevo (más rápido)

      # Paso 4: Compilar el proyecto
      - name: 🔨 Compilar con Maven
        run: mvn compile -B
        # -B = "Batch mode" (no muestra barras de progreso interactivas)
        # Si la compilación falla, el pipeline se detiene aquí ❌
```

#### 🔍 Explicación detallada de cada paso:

**`uses: actions/checkout@v4`**
```
¿Qué hace? Descarga tu código fuente en la máquina virtual
¿Por qué? La máquina virtual empieza vacía — no tiene tu código
Analogía: Es como llegar a una cocina vacía y primero traer los ingredientes
```

**`uses: actions/setup-java@v4`**
```
¿Qué hace? Instala Java 17 en la máquina virtual
¿Por qué? La máquina no tiene Java preinstalado
Analogía: Antes de cocinar, necesitas que la cocina tenga estufa y ollas
```

**`uses: actions/cache@v4`**
```
¿Qué hace? Guarda las dependencias Maven descargadas para reutilizarlas
¿Por qué? Descargar 200 MB de dependencias en cada pipeline es lento
Analogía: En vez de ir al supermercado cada vez, guardas los ingredientes 
          no perecederos en la despensa
```

**`run: mvn compile -B`**
```
¿Qué hace? Ejecuta Maven para compilar tu código Java
¿Por qué? Si el código tiene errores de sintaxis, la compilación falla
Analogía: Intentar ensamblar todas las piezas para ver si encajan
```

---

### Actividad 3 — Hacer Push y Ver tu Pipeline en Acción

```bash
# Agrega el archivo del pipeline
git add .github/workflows/ci.yml

# Haz commit
git commit -m "ci: agregar pipeline básico de compilación"

# Sube al repositorio
git push origin main
```

**Ahora ve a GitHub:**

1. Abre tu repositorio en el navegador.
2. Haz clic en la pestaña **"Actions"** (está arriba, junto a "Pull requests").
3. Verás tu pipeline ejecutándose (ícono amarillo ⏳ = en progreso).
4. Haz clic en el nombre del workflow para ver los detalles.
5. Haz clic en el job **"🔨 Compilar Proyecto"** para ver los logs paso a paso.

**Si todo sale bien**, verás un ✅ verde. **Si algo falla**, verás un ❌ rojo con los logs del error.

> 🎉 **¡Felicidades!** Acabas de crear tu primer pipeline de CI. Desde ahora, CADA vez que hagas push, GitHub compilará tu proyecto automáticamente.

---

### Actividad 4 — Provocar un Fallo (Intencionalmente)

Es importante saber cómo se ve un **pipeline fallido**. Introduce un error a propósito:

1. Abre cualquier archivo `.java` y escribe algo que no compile:
```java
// En LibroController.java, agrega esto:
public void metodoRoto() {
    int x = "esto no es un número"; // ERROR de compilación
}
```

2. Haz commit y push:
```bash
git add .
git commit -m "test: introducir error intencional para ver pipeline rojo"
git push origin main
```

3. Ve a la pestaña **Actions** en GitHub. Verás:
   - El pipeline arranca (⏳ amarillo)
   - El paso "Compilar con Maven" falla (❌ rojo)
   - Si haces clic, verás el error exacto en los logs

4. **Ahora corrige el error**, haz commit y push de nuevo:
```bash
# Elimina el código roto
git add .
git commit -m "fix: corregir error de compilación"
git push origin main
```

5. El pipeline debería volver a ponerse verde ✅.

> 🔑 **Esto es exactamente lo que pasa en la industria.** Si un desarrollador sube código que no compila, el pipeline lo detecta inmediatamente. El equipo puede ver quién rompió el build y qué commit lo causó.

---

### Actividad 5 — Agregar el Paso de Tests al Pipeline

Si ya tienes tests en tu proyecto (aunque sea uno solo), agrega este paso al pipeline:

```yaml
      # Paso 5: Ejecutar las pruebas automatizadas
      - name: 🧪 Ejecutar Tests
        run: mvn test -B
        # Si algún test falla, el pipeline se detiene aquí ❌
```

Tu archivo `ci.yml` completo quedaría así:

```yaml
name: LibroTech CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    name: 🔨 Build & Test
    runs-on: ubuntu-latest

    steps:
      - name: 📥 Checkout del código
        uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Cachear dependencias Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: 🔨 Compilar con Maven
        run: mvn compile -B

      - name: 🧪 Ejecutar Tests
        run: mvn test -B
```

> 💡 **¿No tienes tests todavía?** No hay problema. Maven con `mvn test` también compila. Si no hay tests, simplemente mostrará `Tests run: 0`. No falla.

---

### Actividad 6 — Agregar el Badge de Estado

Un **badge** (insignia) es una imagen dinámica que muestra el estado de tu pipeline. Se pone en el `README.md` del proyecto para que cualquiera vea de un vistazo si el build está pasando.

Agrega esto al inicio de tu `README.md`:

```markdown
# LibroTech 📚

![CI Status](https://github.com/TU_USUARIO/librotech-springboot/actions/workflows/ci.yml/badge.svg)

Sistema de gestión de biblioteca desarrollado con Spring Boot.
```

> 📌 Reemplaza `TU_USUARIO` con tu nombre de usuario real de GitHub.

El badge mostrará automáticamente:
- 🟢 **passing** — El último pipeline pasó correctamente.
- 🔴 **failing** — El último pipeline falló.

---

### Actividad 7 — Práctica Autónoma

1. **Agrega una rama `develop`**: Crea la rama, haz un cambio pequeño, haz push, y verifica que el pipeline también se ejecuta en esa rama.
2. **Crea un Pull Request**: Desde `develop` hacia `main`. Observa cómo GitHub ejecuta el pipeline automáticamente en el PR y muestra si los checks pasaron.
3. **Investiga el consumo de minutos**: Ve a Settings → Billing en tu cuenta de GitHub. Mira cuántos minutos de Actions has usado.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El proyecto está subido a GitHub | ☐ |
| Existe el archivo `.github/workflows/ci.yml` | ☐ |
| El pipeline se ejecuta automáticamente con cada push | ☐ |
| El paso de compilación (`mvn compile`) pasa correctamente | ☐ |
| Se entiende cómo leer los logs del pipeline en GitHub | ☐ |
| Se agregó el badge de estado al `README.md` | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si dos desarrolladores hacen push al mismo tiempo? ¿Se ejecutarían dos pipelines en paralelo?
2. ¿Por qué es útil cachear las dependencias de Maven? ¿Cuánto tiempo ahorraría aproximadamente?
3. ¿Qué información te da el badge de estado que no te daría simplemente abrir el repositorio?
4. Si tu pipeline tarda 3 minutos en ejecutarse y haces 20 pushes al día, ¿cuántos minutos de GitHub Actions usarías al mes?

---

## ➡️ Siguiente Fase

Una vez tu pipeline básico está funcionando, avanza a **[Fase 2 — Pipeline con Testing y Reportes](./02-Fase2-Pipeline-Testing-Reportes.md)**, donde agregarás ejecución de tests, reportes de resultados y cobertura de código al pipeline.
