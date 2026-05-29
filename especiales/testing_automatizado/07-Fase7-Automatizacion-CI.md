# ⚙️ FASE 7 — Automatización con Maven Profiles y Conceptos de CI/CD

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 45 minutos  
**Nivel:** Avanzado  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Separar tus tests en **perfiles de Maven** (rápidos vs completos).
2. Configurar la ejecución selectiva de tests con **Maven Surefire Plugin**.
3. Entender cómo los tests automatizados se integran en un **pipeline de CI/CD**.
4. Crear un script que automatice la validación completa del proyecto.
5. Conocer el concepto de **regresión** y cómo los tests automáticos la previenen.

---

## 🧠 ¿Qué es CI/CD y cómo se relaciona con los tests?

**CI** = **Continuous Integration** (Integración Continua)  
**CD** = **Continuous Delivery/Deployment** (Entrega/Despliegue Continuo)

En un equipo de desarrollo profesional, el flujo es este:

```
 Desarrollador              Servidor CI                  Producción
 hace commit    ───────►   (GitHub Actions,    ───────►  Se despliega
 y push                     Jenkins, etc.)               automáticamente
                               │
                               ├── 1. Descarga el código
                               ├── 2. Compila (mvn compile)
                               ├── 3. Ejecuta TODOS los tests (mvn test) ← ¡Aquí es donde brillan!
                               ├── 4. Genera reporte de cobertura
                               ├── 5. Si TODO pasa → ✅ Deploy
                               └── 6. Si algo falla → ❌ Alerta al equipo
```

### Analogía

Es como una fábrica de automóviles:
- Cada vez que un ingeniero cambia un diseño (commit), el auto pasa por una **línea de inspección automatizada** (CI).
- Si alguna pieza no encaja (test falla), la línea se **detiene** y se alerta al ingeniero.
- Si todo pasa, el auto se envía al concesionario (deploy a producción).

Sin esa línea de inspección, autos defectuosos llegarían a los clientes.

---

## 📖 Contexto de Negocio

**LibroTech** tiene ahora un equipo de 5 desarrolladores. Cada uno puede hacer cambios en el código. Sin CI/CD, cada cambio es una apuesta: "espero no haber roto nada". Con CI/CD y tests automatizados, cada cambio se **valida automáticamente** antes de llegar a producción.

---

## 📝 Actividades

### Actividad 1 — Separar Tests Rápidos y Lentos con Tags de JUnit

En proyectos grandes, no siempre quieres ejecutar TODOS los tests. Los tests de integración (Fase 5) son lentos. A veces solo quieres correr los unitarios para validar rápidamente.

**Paso 1: Etiqueta tus tests de integración:**

```java
import org.junit.jupiter.api.Tag;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")  // ← Etiqueta para identificar este grupo
class LibroIntegrationTest {
    // ... tus tests de integración ...
}
```

**Paso 2: Etiqueta tus tests unitarios:**

```java
@ExtendWith(MockitoExtension.class)
@Tag("unit")  // ← Etiqueta para tests unitarios
class LibroServiceTest {
    // ... tus tests unitarios ...
}
```

---

### Actividad 2 — Configurar Maven Surefire para Ejecución Selectiva

Agrega esta configuración en tu `pom.xml` dentro de `<build><plugins>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <!-- Por defecto, excluye los tests de integración (son lentos) -->
        <excludedGroups>integration</excludedGroups>
    </configuration>
</plugin>
```

Ahora crea un **perfil** para ejecutar TODO (unitarios + integración):

```xml
<profiles>
    <!-- Perfil para ejecutar TODOS los tests (incluye integración) -->
    <profile>
        <id>all-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <!-- Quita la exclusión: ejecuta TODOS los tags -->
                        <excludedGroups></excludedGroups>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>

    <!-- Perfil para ejecutar SOLO integración -->
    <profile>
        <id>integration-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <groups>integration</groups>
                        <excludedGroups></excludedGroups>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

#### Ahora puedes ejecutar tests selectivamente:

```bash
# Solo tests unitarios (rápido — ideal para desarrollo)
mvn test

# TODOS los tests (unitarios + integración)
mvn test -P all-tests

# Solo tests de integración
mvn test -P integration-tests
```

#### 🔍 ¿Cuándo usar cada uno?

| Comando | ¿Cuándo? | Tiempo estimado |
|---|---|---|
| `mvn test` | Mientras desarrollas, cada 5 minutos | ~5 segundos |
| `mvn test -P all-tests` | Antes de hacer push al repositorio | ~30 segundos |
| `mvn test -P integration-tests` | En el servidor CI automáticamente | ~20 segundos |

---

### Actividad 3 — Crear un Script de Validación Completa

Crea un archivo en la raíz de tu proyecto llamado `validate.sh` (o `validate.bat` en Windows):

**Linux/Mac — `validate.sh`:**
```bash
#!/bin/bash
echo "🔍 === VALIDACIÓN COMPLETA DE LIBROTECH ==="
echo ""

echo "📦 Paso 1: Limpiando y compilando..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ ERROR: El proyecto no compila."
    exit 1
fi
echo "✅ Compilación exitosa"
echo ""

echo "🧪 Paso 2: Ejecutando tests unitarios..."
mvn test -q
if [ $? -ne 0 ]; then
    echo "❌ ERROR: Algunos tests unitarios fallaron."
    exit 1
fi
echo "✅ Tests unitarios pasaron"
echo ""

echo "🔗 Paso 3: Ejecutando tests de integración..."
mvn test -P all-tests -q
if [ $? -ne 0 ]; then
    echo "❌ ERROR: Algunos tests de integración fallaron."
    exit 1
fi
echo "✅ Tests de integración pasaron"
echo ""

echo "📊 Paso 4: Generando reporte de cobertura..."
echo "   Reporte disponible en: target/site/jacoco/index.html"
echo ""

echo "🎉 === VALIDACIÓN COMPLETA: TODO OK ==="
```

Dale permisos de ejecución y ejecútalo:
```bash
chmod +x validate.sh
./validate.sh
```

---

### Actividad 4 — Entender la Regresión (y por qué los tests la previenen)

**Regresión** = Cuando un cambio nuevo **rompe algo que antes funcionaba**.

Ejemplo real en LibroTech:

```
Lunes:    El método guardar() funciona perfectamente. ✅
Martes:   Un desarrollador cambia la validación del ISBN.
Miércoles: Un usuario reporta que no puede guardar libros. ❌
          El cambio del martes rompió algo sin querer.
```

Con tests automatizados:

```
Lunes:    El método guardar() funciona. Tests pasan. ✅
Martes:   Un desarrollador cambia la validación del ISBN.
          Ejecuta "mvn test" → test guardarDeberiaAsignarId() FALLA ❌
          El desarrollador se da cuenta INMEDIATAMENTE del problema.
          Lo corrige antes de hacer push.
Miércoles: Todo sigue funcionando. ✅
```

**Cada test que escribiste en las Fases 1-5 es una barrera contra la regresión.**

---

### Actividad 5 — Concepto de GitHub Actions (Referencia)

Aunque la configuración de un servidor CI real está fuera del alcance de este laboratorio, es importante que entiendas cómo se vería. Este es un ejemplo de archivo de configuración para **GitHub Actions**:

```yaml
# .github/workflows/ci.yml
name: LibroTech CI

# Se ejecuta cada vez que alguien hace push o abre un Pull Request
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      # 1. Descarga el código
      - uses: actions/checkout@v4

      # 2. Instala Java 17
      - name: Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      # 3. Ejecuta todos los tests
      - name: Ejecutar tests
        run: mvn test -P all-tests

      # 4. Genera reporte de cobertura
      - name: Generar reporte JaCoCo
        run: mvn jacoco:report

      # 5. Publica el reporte como artefacto
      - name: Subir reporte de cobertura
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/
```

> 💡 **No necesitas implementar esto ahora.** El objetivo es que entiendas que los tests que escribiste en las fases anteriores son los que un servidor CI ejecutaría automáticamente cada vez que alguien hace push.

---

### Actividad 6 — Resumen Final: Tu Arsenal Completo de Testing

Felicidades 🎉. Si completaste las 7 fases, tu proyecto LibroTech ahora tiene:

```
📊 RESUMEN DE LA SUITE DE TESTING DE LIBROTECH
═══════════════════════════════════════════════════

Fase 1 — Tests del Modelo (JUnit puro)
  ├── LibroTest.java               → ~5 tests
  └── EditorialTest.java           → ~3 tests

Fase 2 — Tests del Servicio (Mockito)
  └── LibroServiceTest.java        → ~8 tests

Fase 3 — Tests del Repositorio (@DataJpaTest)
  └── LibroRepositoryTest.java     → ~6 tests

Fase 4 — Tests del Controlador (MockMvc)
  └── LibroControllerTest.java     → ~8 tests

Fase 5 — Tests de Integración (@SpringBootTest)
  └── LibroIntegrationTest.java    → ~5 tests

──────────────────────────────────────────────────
Total aproximado: 35+ tests automatizados
Cobertura estimada: 60-80%
Tiempo de ejecución: ~20-40 segundos
═══════════════════════════════════════════════════

Ejecución: mvn clean test -P all-tests
Reporte:   target/site/jacoco/index.html
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se configuraron tags `@Tag("unit")` y `@Tag("integration")` | ☐ |
| Se crearon perfiles Maven para ejecución selectiva | ☐ |
| `mvn test` ejecuta solo tests unitarios | ☐ |
| `mvn test -P all-tests` ejecuta todos los tests | ☐ |
| Se creó el script de validación `validate.sh` | ☐ |
| Se entiende el concepto de regresión y cómo prevenirla | ☐ |

---

## 🔍 Preguntas de Reflexión Final

1. Si tu equipo tiene un servidor CI que ejecuta `mvn test` automáticamente con cada push, ¿qué pasaría si un desarrollador sube código que rompe un test?
2. ¿Por qué es útil separar los tests unitarios de los de integración? ¿Qué pasa si un servidor CI ejecuta todos los tests en cada commit y tardan 10 minutos?
3. Imagina que eres el líder técnico de LibroTech y recibes un Pull Request con 200 líneas de código nuevo pero 0 tests. ¿Lo aprobarías? ¿Por qué?
4. ¿Cómo te ayudaron los tests a entender mejor tu propio código de LibroTech?

---

## 🏁 Conclusión del Laboratorio Especial

Has recorrido las 7 fases del testing automatizado en Spring Boot:

| Fase | Concepto Clave | Herramienta |
|---|---|---|
| 1 | Fundamentos: aserciones y estructura | JUnit 5 |
| 2 | Aislamiento con objetos simulados | Mockito |
| 3 | Verificación de persistencia | @DataJpaTest |
| 4 | Validación de API REST | MockMvc |
| 5 | Flujo completo de extremo a extremo | @SpringBootTest |
| 6 | Medición y calidad | JaCoCo |
| 7 | Automatización y CI/CD | Maven Profiles |

La clave final es esta: **los tests no son una tarea extra que haces "si te queda tiempo"**. Son una parte fundamental del desarrollo profesional de software. Un código sin tests es un código en el que no se puede confiar.

> 🧠 *"El código que no se puede probar es código que no se puede mantener."*
> — Michael Feathers, "Working Effectively with Legacy Code"

---

## 🚀 ¿Quieres profundizar en CI/CD?

Si quieres implementar un pipeline CI/CD completo de verdad (no solo los conceptos), revisa el **Laboratorio Especial de CI/CD** en:

📂 `especiales/cicd_springboot/`

Allí aprenderás paso a paso a configurar **GitHub Actions**, crear pipelines con múltiples stages, manejar **Docker**, secretos, ambientes y protección de ramas. Se puede implementar **desde la Semana 1** del módulo.
