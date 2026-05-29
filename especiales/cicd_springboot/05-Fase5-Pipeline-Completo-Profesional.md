# 🚀 FASE 5 — Pipeline Profesional Completo

**Laboratorio Especial de CI/CD · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Avanzado  
**Aplica desde:** Semana 4 (proyecto maduro)  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Construir un **pipeline profesional completo** con múltiples stages.
2. Configurar **protección de ramas** (Branch Protection Rules) en GitHub.
3. Implementar una estrategia de **Gitflow** integrada con CI/CD.
4. Publicar la imagen Docker en **GitHub Container Registry (GHCR)**.
5. Entender el flujo completo **desde el commit hasta producción**.

---

## 🧠 El Pipeline Profesional: Visión Completa

Este es el pipeline que vamos a construir. Representa lo que encontrarías en una empresa de desarrollo de software:

```
                    Developer hace push
                           │
                           ▼
               ┌──────────────────────┐
               │  📥 Checkout código  │
               └──────────┬───────────┘
                           │
                           ▼
               ┌──────────────────────┐
               │  🔨 Compilar (Maven) │──── Si falla → ❌ STOP
               └──────────┬───────────┘
                           │
                    ┌──────┴──────┐
                    │             │
                    ▼             ▼
           ┌──────────────┐ ┌──────────────┐
           │ 🧪 Unit Tests│ │ 🔗 Integ     │──── Si falla → ❌ STOP
           │   (rápidos)  │ │   Tests      │
           └──────┬───────┘ └──────┬───────┘
                   │               │
                   └───────┬───────┘
                           ▼
               ┌──────────────────────┐
               │ 📊 Cobertura JaCoCo  │──── Si < 50% → ❌ STOP
               └──────────┬───────────┘
                           │
                           ▼
               ┌──────────────────────┐
               │ 🐳 Docker Build     │
               └──────────┬───────────┘
                           │
                    (Solo en main)
                           │
                           ▼
               ┌──────────────────────┐
               │ 📤 Push a Registry   │
               └──────────┬───────────┘
                           │
                           ▼
               ┌──────────────────────┐
               │ 🚀 Deploy (manual)  │
               └──────────────────────┘
```

---

## 📖 Contexto de Negocio

**LibroTech** ya tiene un equipo de 5 desarrolladores. El CTO establece las siguientes reglas:

1. **Nadie** hace push directamente a `main`. Todo va por Pull Request.
2. Un PR no se puede mergear si el pipeline está en rojo.
3. Cada merge a `main` genera una imagen Docker lista para producción.
4. La imagen se publica automáticamente en un registro de contenedores.

---

## 📝 Actividades

### Actividad 1 — Pipeline Profesional Completo

Reemplaza tu `.github/workflows/ci.yml` con este pipeline completo:

```yaml
# ============================================================
# 🚀 Pipeline CI/CD Profesional — LibroTech
# ============================================================
name: LibroTech CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

# Permisos necesarios para publicar paquetes Docker
permissions:
  contents: read
  packages: write

jobs:
  # ──────────────────────────────────────────
  # STAGE 1: Compilar el proyecto
  # ──────────────────────────────────────────
  compile:
    name: 🔨 Compilar
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven  # ← Forma simplificada de cachear Maven

      - name: Compilar proyecto
        run: mvn compile -B -q

  # ──────────────────────────────────────────
  # STAGE 2: Tests Unitarios (paralelo)
  # ──────────────────────────────────────────
  unit-tests:
    name: 🧪 Tests Unitarios
    runs-on: ubuntu-latest
    needs: compile
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Ejecutar Tests Unitarios
        run: mvn test -B -Dgroups="unit"
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: Publicar Resultados
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-results
          path: target/surefire-reports/
          retention-days: 7

  # ──────────────────────────────────────────
  # STAGE 2b: Tests de Integración (paralelo)
  # ──────────────────────────────────────────
  integration-tests:
    name: 🔗 Tests de Integración
    runs-on: ubuntu-latest
    needs: compile
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Ejecutar Tests de Integración
        run: mvn test -B -Dgroups="integration"
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: Publicar Resultados
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: integration-test-results
          path: target/surefire-reports/
          retention-days: 7

  # ──────────────────────────────────────────
  # STAGE 3: Cobertura de Código
  # ──────────────────────────────────────────
  coverage:
    name: 📊 Cobertura
    runs-on: ubuntu-latest
    needs: [unit-tests, integration-tests]  # Espera ambos tipos de tests
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Generar Reporte de Cobertura
        run: mvn verify -B
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: Publicar Reporte JaCoCo
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: target/site/jacoco/
          retention-days: 14

  # ──────────────────────────────────────────
  # STAGE 4: Construir y Publicar Imagen Docker
  # (Solo en pushes a main — no en PRs ni develop)
  # ──────────────────────────────────────────
  docker-publish:
    name: 🐳 Docker Build & Publish
    runs-on: ubuntu-latest
    needs: coverage
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'

    steps:
      - uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      # Autenticarse en GitHub Container Registry
      - name: 🔐 Login en GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
          # GITHUB_TOKEN se genera automáticamente — no necesitas crear este secreto

      # Construir la imagen Docker
      - name: 🐳 Construir Imagen
        run: |
          docker build -t ghcr.io/${{ github.repository }}:${{ github.sha }} .
          docker tag ghcr.io/${{ github.repository }}:${{ github.sha }} \
                     ghcr.io/${{ github.repository }}:latest

      # Publicar la imagen en GHCR
      - name: 📤 Publicar Imagen
        run: |
          docker push ghcr.io/${{ github.repository }}:${{ github.sha }}
          docker push ghcr.io/${{ github.repository }}:latest

      - name: ✅ Resumen
        run: |
          echo "## 🐳 Imagen Docker Publicada" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "- **Registry:** ghcr.io/${{ github.repository }}" >> $GITHUB_STEP_SUMMARY
          echo "- **Tag:** ${{ github.sha }}" >> $GITHUB_STEP_SUMMARY
          echo "- **Latest:** ghcr.io/${{ github.repository }}:latest" >> $GITHUB_STEP_SUMMARY
```

#### 🔍 Conceptos nuevos:

**Jobs en paralelo:**
```yaml
unit-tests:
  needs: compile        # Ambos esperan SOLO a compile
integration-tests:
  needs: compile        # Se ejecutan al MISMO tiempo ⚡

coverage:
  needs: [unit-tests, integration-tests]  # Espera a que AMBOS terminen
```

**`if: github.ref == 'refs/heads/main'`**
```
Solo ejecuta este job cuando el push es a la rama main.
Los PRs y pushes a develop NO construyen Docker.
```

**GitHub Container Registry (GHCR):**
```
Es un registro de imágenes Docker integrado en GitHub.
Es gratuito para repos públicos.
Tu imagen queda en: ghcr.io/tu-usuario/librotech-springboot:latest
```

---

### Actividad 2 — Configurar Protección de Ramas

La protección de ramas impide que alguien haga push directo a `main` sin pasar por un PR con pipeline verde.

1. Ve a tu repositorio → **Settings** → **Branches**.
2. Haz clic en **"Add branch protection rule"**.
3. En "Branch name pattern", escribe: `main`.
4. Marca las siguientes opciones:

| Opción | ¿Para qué? |
|---|---|
| ✅ **Require a pull request before merging** | Nadie puede hacer push directo a main |
| ✅ **Require status checks to pass before merging** | El pipeline debe estar verde para mergear |
| ✅ **Require branches to be up to date before merging** | La rama debe tener los últimos cambios de main |

5. En "Status checks that are required", selecciona:
   - `🧪 Tests Unitarios`
   - `🔗 Tests de Integración`
   - `📊 Cobertura`

6. Haz clic en **"Create"**.

> 🔑 **Resultado**: Ahora es IMPOSIBLE que código sin tests pasando llegue a la rama principal. El pipeline actúa como un **portero** que no deja pasar código roto.

---

### Actividad 3 — Flujo Gitflow con CI/CD

El flujo de trabajo profesional con CI/CD se ve así:

```
1. Crear rama feature desde develop:
   git checkout develop
   git checkout -b feature/busqueda-avanzada

2. Desarrollar y hacer commits:
   git add .
   git commit -m "feat: agregar búsqueda por título parcial"
   git push origin feature/busqueda-avanzada
   → Pipeline se ejecuta en la rama ✅

3. Abrir Pull Request: feature/busqueda-avanzada → develop
   → Pipeline se ejecuta en el PR ✅
   → Un compañero revisa el código (Code Review)
   → Si todo verde → Merge ✅

4. Abrir Pull Request: develop → main
   → Pipeline completo se ejecuta ✅
   → Si todo verde → Merge ✅
   → Docker Build & Publish se ejecuta automáticamente 🐳

5. La imagen Docker está lista para desplegar 🚀
```

---

### Actividad 4 — Agregar el Step Summary

GitHub Actions permite generar un **resumen visual** al final de cada ejecución:

```yaml
      - name: 📋 Resumen del Pipeline
        if: always()
        run: |
          echo "## 📋 Resumen del Build" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "| Stage | Estado |" >> $GITHUB_STEP_SUMMARY
          echo "|-------|--------|" >> $GITHUB_STEP_SUMMARY
          echo "| Compilación | ✅ |" >> $GITHUB_STEP_SUMMARY
          echo "| Tests Unitarios | ✅ |" >> $GITHUB_STEP_SUMMARY
          echo "| Tests Integración | ✅ |" >> $GITHUB_STEP_SUMMARY
          echo "| Cobertura | ✅ |" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "**Commit:** \`${{ github.sha }}\`" >> $GITHUB_STEP_SUMMARY
          echo "**Branch:** ${{ github.ref_name }}" >> $GITHUB_STEP_SUMMARY
          echo "**Author:** ${{ github.actor }}" >> $GITHUB_STEP_SUMMARY
```

Este resumen aparece en la pestaña **"Summary"** de cada ejecución del pipeline en GitHub.

---

### Actividad 5 — Verificar Todo el Flujo

Realiza este ejercicio completo:

1. **Crea una rama** `feature/test-pipeline` desde `develop`.
2. **Haz un cambio** (puede ser agregar un test nuevo).
3. **Push** a la rama.
4. **Abre un PR** hacia `develop`.
5. **Verifica** que el pipeline se ejecuta automáticamente en el PR.
6. **Merge** el PR (si está verde).
7. **Abre otro PR** desde `develop` hacia `main`.
8. **Merge** y verifica que Docker Build & Publish se ejecuta.
9. **Ve a la pestaña Packages** de tu repositorio en GitHub → deberías ver tu imagen Docker publicada.

---

### Actividad 6 — Resumen Final: Tu Pipeline Profesional

```
📊 RESUMEN DEL PIPELINE CI/CD DE LIBROTECH
═══════════════════════════════════════════════════

🔨 Stage 1: Compilación
   └── mvn compile

🧪 Stage 2: Tests (paralelo)
   ├── Unit Tests (-Dgroups="unit")
   └── Integration Tests (-Dgroups="integration")

📊 Stage 3: Cobertura
   └── JaCoCo verify (mínimo 50%)

🐳 Stage 4: Docker (solo en main)
   ├── docker build
   └── docker push → ghcr.io

🔒 Protección de Ramas:
   └── main requiere PR + pipeline verde

⏱️ Tiempo total del pipeline: ~3-5 minutos
═══════════════════════════════════════════════════
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El pipeline tiene 4+ stages separados | ☐ |
| Los tests unitarios e integración se ejecutan en paralelo | ☐ |
| Docker Build solo se ejecuta en pushes a `main` | ☐ |
| La rama `main` tiene reglas de protección configuradas | ☐ |
| La imagen Docker se publica en GitHub Container Registry | ☐ |
| Se entiende el flujo completo desde commit hasta imagen publicada | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Cuánto cuesta en minutos de GitHub Actions ejecutar este pipeline completo? Si tu equipo hace 30 merges al día, ¿se quedarían sin minutos gratuitos?
2. ¿Qué pasaría si un desarrollador intenta hacer `git push origin main` directamente con las Branch Protection Rules activas?
3. ¿Por qué Docker Build solo se ejecuta en `main` y no en todas las ramas? ¿Qué problema evita esto?
4. Si mañana tu empresa decide migrar de GitHub Actions a Jenkins, ¿qué partes del pipeline se reutilizarían y cuáles tendrías que reescribir?

---

## 🏁 Conclusión del Laboratorio de CI/CD

Has recorrido las 5 fases de CI/CD para aplicaciones Java + Spring Boot:

| Fase | Concepto | Herramienta | ¿Desde cuándo? |
|---|---|---|---|
| 1 | Primer pipeline CI | GitHub Actions | Semana 1 |
| 2 | Tests y reportes en CI | Surefire + JaCoCo | Semana 2 |
| 3 | Ambientes y secretos | Spring Profiles + Secrets | Semana 2 |
| 4 | Containerización | Docker | Semana 3 |
| 5 | Pipeline profesional | GHCR + Branch Protection | Semana 4 |

La lección más importante es esta: **CI/CD no es algo que se agrega al final del proyecto. Se configura al principio y crece con él.** Cada fase que completaste aquí se puede aplicar desde la primera semana del módulo.

> 🧠 *"Si te duele hacerlo manualmente, automatízalo."*  
> — Principio DevOps
