# 🚀 FASE 2 — Pipeline con Testing Automatizado y Reportes

**Laboratorio Especial de CI/CD · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Intermedio  
**Aplica desde:** Semana 2 (cuando ya tienes tests con JUnit/Mockito)  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Configurar un pipeline que ejecute tests unitarios y de integración **separadamente**.
2. Publicar los **resultados de los tests** como artefactos descargables en GitHub.
3. Agregar **reportes de cobertura de código** (JaCoCo) al pipeline.
4. Hacer que el pipeline **falle si la cobertura es menor al umbral mínimo**.
5. Usar **jobs paralelos** para acelerar la ejecución del pipeline.

---

## 🧠 ¿Por qué separar Build, Tests y Reportes?

En la Fase 1 teníamos todo en un solo job. Pero a medida que el proyecto crece, es mejor separar las responsabilidades:

```
Pipeline Fase 1 (todo junto):
┌───────────────────────────────────────────┐
│ Job: build                                │
│ compile → test → (todo en 1 solo bloque)  │
└───────────────────────────────────────────┘

Pipeline Fase 2 (separado por responsabilidad):
┌──────────┐   ┌───────────┐   ┌──────────────┐
│ compile  │──►│ unit-tests│──►│ coverage     │
│ 🔨       │   │ 🧪        │   │ 📊           │
└──────────┘   └───────────┘   └──────────────┘
```

**Ventajas:**
- Si la compilación falla, no pierdes tiempo ejecutando tests.
- Puedes ver rápidamente QUÉ fase falló (compilación vs tests vs cobertura).
- Los jobs independientes pueden ejecutarse en paralelo.

---

## 📖 Contexto de Negocio

El equipo de QA de **LibroTech** exige que cada Pull Request muestre:
1. Si todos los tests pasaron ✅ o cuáles fallaron ❌.
2. El reporte de cobertura: qué porcentaje del código está cubierto.
3. Un umbral mínimo: si la cobertura baja del 50%, el PR se bloquea.

---

## 📝 Actividades

### Actividad 1 — Pipeline con Jobs Separados

Reemplaza tu `.github/workflows/ci.yml` con esta versión mejorada:

```yaml
name: LibroTech CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  # ────────────────────────────────────────
  # Job 1: Compilar el proyecto
  # ────────────────────────────────────────
  compile:
    name: 🔨 Compilar
    runs-on: ubuntu-latest

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: 🔨 Compilar
        run: mvn compile -B -q
        # -q = "quiet mode" (menos output en los logs)

  # ────────────────────────────────────────
  # Job 2: Ejecutar Tests
  # ────────────────────────────────────────
  test:
    name: 🧪 Tests
    runs-on: ubuntu-latest
    needs: compile  # ← Espera a que "compile" termine exitosamente

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: 🧪 Ejecutar Tests
        run: mvn test -B

      # Subir los resultados de los tests como artefacto
      - name: 📄 Publicar Resultados de Tests
        if: always()  # ← Se ejecuta INCLUSO si el paso anterior falla
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/
          retention-days: 7
          # Los reportes se guardan 7 días en GitHub

  # ────────────────────────────────────────
  # Job 3: Cobertura de Código
  # ────────────────────────────────────────
  coverage:
    name: 📊 Cobertura
    runs-on: ubuntu-latest
    needs: test  # ← Espera a que "test" termine

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: 📊 Generar Reporte de Cobertura
        run: mvn test jacoco:report -B

      # Subir el reporte HTML de JaCoCo como artefacto descargable
      - name: 📄 Publicar Reporte de Cobertura
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: target/site/jacoco/
          retention-days: 14
```

#### 🔍 Conceptos nuevos:

**`needs: compile`**
```yaml
test:
  needs: compile  # "No empezar hasta que 'compile' haya terminado con éxito"
```
Esto crea una **cadena de dependencias**:
```
compile ──► test ──► coverage
   Si falla,    Si falla,
   test no      coverage no
   se ejecuta   se ejecuta
```

**`if: always()`**
```yaml
- name: Publicar Resultados
  if: always()  # Se ejecuta siempre, incluso si el test falló
```
¿Por qué? Porque cuando un test falla, es JUSTO cuando más necesitas ver los reportes. Sin `if: always()`, el paso de publicación se saltaría cuando hay fallos.

**`actions/upload-artifact@v4`**
```yaml
- uses: actions/upload-artifact@v4
  with:
    name: coverage-report    # Nombre del artefacto
    path: target/site/jacoco/  # Carpeta a subir
    retention-days: 14       # Se guarda 14 días
```
Sube archivos como "artefactos" que puedes descargar desde la pestaña Actions de GitHub. Son como adjuntos de email pero en tu pipeline.

---

### Actividad 2 — Ver los Artefactos en GitHub

1. Haz push con el pipeline actualizado.
2. Ve a la pestaña **Actions** en GitHub.
3. Haz clic en la ejecución más reciente.
4. En la parte inferior de la página, verás la sección **"Artifacts"**.
5. Descarga `test-results` y `coverage-report`.
6. Abre el archivo `index.html` del reporte de cobertura en tu navegador.

> 💡 **Ahora cualquier miembro de tu equipo puede descargar el reporte de cobertura desde GitHub** sin necesidad de clonar el proyecto y ejecutar los tests localmente.

---

### Actividad 3 — Agregar Reporte de Tests en el Pull Request

Para que los resultados de tests aparezcan directamente en el Pull Request (sin necesidad de descargar artefactos), agrega esta acción al job `test`:

```yaml
      # Mostrar resultados de tests como comentario en el PR
      - name: 📋 Reportar Tests en PR
        if: always() && github.event_name == 'pull_request'
        uses: dorny/test-reporter@v1
        with:
          name: Resultados de Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit
```

Con esto, cuando abras un Pull Request, verás un resumen directamente en GitHub:

```
✅ Resultados de Tests
  ├── LibroTest            3/3 passed
  ├── LibroServiceTest     8/8 passed
  ├── LibroRepositoryTest  6/6 passed
  └── LibroControllerTest  8/8 passed
  
  Total: 25 passed, 0 failed
```

---

### Actividad 4 — Verificación de Cobertura Mínima en el Pipeline

Si configuraste JaCoCo con un umbral mínimo en tu `pom.xml` (como se vio en la Fase 6 del laboratorio de Testing), el pipeline **fallará automáticamente** si la cobertura es insuficiente.

Agrega o modifica en tu `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <!-- NUEVO: Verificación de cobertura mínima -->
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Luego actualiza el step de cobertura en tu pipeline:

```yaml
      - name: 📊 Verificar Cobertura Mínima (50%)
        run: mvn verify -B
        # mvn verify ejecuta: compile → test → jacoco:check
        # Si la cobertura es < 50%, este paso FALLA ❌
```

> 🔑 **¿Qué se logra con esto?** Si un desarrollador agrega 100 líneas de código nuevo sin un solo test, la cobertura baja. El pipeline falla. El código no entra a `main` hasta que se agreguen tests.

---

### Actividad 5 — Pipeline Optimizado con Job Único (Alternativa)

Si prefieres un pipeline más simple que haga todo en un solo job (ideal para proyectos pequeños), aquí tienes una versión compacta:

```yaml
name: LibroTech CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    name: 🔨 Build, Test & Coverage
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: 🚀 Build + Test + Cobertura
        run: mvn verify -B

      - name: 📄 Publicar Reportes
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: reports
          path: |
            target/surefire-reports/
            target/site/jacoco/
          retention-days: 7
```

> 💡 **`mvn verify`** es un comando que ejecuta TODO: compilar → tests → verificar cobertura → empaquetar. Es el "haz todo" de Maven.

---

### Actividad 6 — Práctica Autónoma

1. **Crea una rama `feature/nueva-funcionalidad`**, haz un cambio, abre un PR hacia `main`, y observa cómo el pipeline se ejecuta automáticamente en el PR.

2. **Rompe un test a propósito**: Modifica un `assertEquals` para que falle, haz push, y descarga el artefacto `test-results` para ver los detalles del fallo.

3. **Baja el umbral de cobertura a 0.80 (80%)**: ¿Tu proyecto pasa? Si no, agrega más tests hasta que pase.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El pipeline tiene jobs separados para compilar, testear y reportar cobertura | ☐ |
| Los resultados de tests se publican como artefactos descargables | ☐ |
| El reporte de cobertura (JaCoCo HTML) se publica como artefacto | ☐ |
| El pipeline falla si la cobertura es inferior al umbral definido | ☐ |
| Se entiende el uso de `needs`, `if: always()` y `upload-artifact` | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué separamos el pipeline en múltiples jobs en vez de hacer todo en uno solo? ¿Cuáles son los pros y contras de cada enfoque?
2. Si un Pull Request tiene el pipeline en rojo (tests fallando), ¿deberías hacer merge de ese PR? ¿Por qué?
3. ¿Qué pasa con los artefactos después de los `retention-days`? ¿Se borran automáticamente?
4. ¿Cómo le explicarías a un compañero de equipo que no sabe de CI/CD por qué el pipeline rechazó su código?

---

## ➡️ Siguiente Fase

Ahora que tu pipeline ejecuta tests y genera reportes, avanza a **[Fase 3 — Ambientes, Secretos y Variables](./03-Fase3-Ambientes-Secretos-Variables.md)**, donde aprenderás a manejar configuraciones diferentes para desarrollo, testing y producción.
