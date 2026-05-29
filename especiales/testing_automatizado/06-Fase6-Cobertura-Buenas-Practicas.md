# ⚙️ FASE 6 — Cobertura de Código con JaCoCo y Buenas Prácticas

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 1 hora  
**Nivel:** Intermedio-Avanzado  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Configurar **JaCoCo** para medir qué porcentaje de tu código está cubierto por tests.
2. Leer e interpretar el **reporte HTML de cobertura**.
3. Entender qué significa el porcentaje de cobertura y cuánto es "suficiente".
4. Aplicar las **mejores prácticas** para escribir tests mantenibles.
5. Organizar tu suite de tests de forma profesional.

---

## 🧠 ¿Qué es la Cobertura de Código?

La cobertura de código responde una pregunta simple: **"¿Cuántas líneas de mi código se ejecutan cuando corro mis tests?"**

### Ejemplo

Si tu `LibroService` tiene 50 líneas de código y tus tests ejecutan 40 de ellas, tu cobertura es del **80%**.

```
Total de líneas del servicio: 50
Líneas ejecutadas por tests:  40
Cobertura: 40/50 = 80%

Líneas NO cubiertas: 10 → Estas líneas podrían tener bugs que nunca detectarás
```

### Analogía

Es como revisar un edificio de seguridad:
- **100% cobertura** = Revisaste cada habitación, cada pasillo, cada puerta.
- **50% cobertura** = Solo revisaste la mitad. ¿Y si hay un problema en la mitad que no revisaste?
- **0% cobertura** = No revisaste nada. Estás confiando en la suerte.

> ⚠️ **Advertencia importante**: 100% de cobertura **NO garantiza** que tu código esté libre de bugs. Solo significa que todas las líneas se ejecutaron al menos una vez. Puedes tener 100% de cobertura con tests que no verifican nada útil. La calidad de las aserciones importa tanto como la cobertura.

---

## 📖 Contexto de Negocio

El equipo de calidad de **LibroTech** quiere responder esta pregunta al director de tecnología: *"¿Qué tan protegido está nuestro código contra errores?"*. La cobertura de código no es la respuesta completa, pero es un indicador muy útil. Un proyecto con 10% de cobertura es mucho más riesgoso que uno con 70%.

---

## 📝 Actividades

### Actividad 1 — Configurar JaCoCo en tu proyecto

Agrega el plugin de JaCoCo en tu `pom.xml`, dentro de la sección `<build><plugins>`:

```xml
<build>
    <plugins>
        <!-- ... otros plugins existentes ... -->
        
        <!-- Plugin JaCoCo para cobertura de código -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.12</version>
            <executions>
                <!-- Paso 1: Preparar el agente de JaCoCo antes de los tests -->
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <!-- Paso 2: Generar el reporte después de los tests -->
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 🔍 ¿Qué hace cada parte?

- **`prepare-agent`**: Antes de ejecutar los tests, JaCoCo "se conecta" al proceso de Java para rastrear qué líneas se ejecutan.
- **`report`**: Después de ejecutar los tests, genera un reporte HTML con los resultados.

---

### Actividad 2 — Generar y Leer el Reporte de Cobertura

Ejecuta tus tests con Maven:

```bash
mvn clean test
```

Después de que los tests terminen, JaCoCo habrá generado el reporte en:

```
target/site/jacoco/index.html
```

Ábrelo en tu navegador. Verás algo como esto:

```
┌──────────────────────────────────────────────────┐
│ Package              │ Lines   │ Branches │ Cover │
├──────────────────────────────────────────────────┤
│ com.librotech.model      │ 45/50  │ 0/0     │  90% │
│ com.librotech.service    │ 30/42  │ 6/8     │  71% │
│ com.librotech.controller │ 28/35  │ 4/6     │  80% │
│ com.librotech.repository │  8/8   │ 0/0     │ 100% │
└──────────────────────────────────────────────────┘
```

#### ¿Qué significan las columnas?

| Columna | Significado |
|---|---|
| **Lines** | Líneas de código cubiertas / total de líneas |
| **Branches** | Ramas condicionales cubiertas (if/else) / total de ramas |
| **Cover** | Porcentaje total de cobertura |

Al hacer clic en un paquete, puedes ver el detalle por clase. Y al hacer clic en una clase, verás el código fuente coloreado:

- 🟢 **Verde** = Esta línea fue ejecutada por al menos un test.
- 🔴 **Rojo** = Esta línea NUNCA fue ejecutada. Posible punto ciego.
- 🟡 **Amarillo** = Esta rama (if/else) fue parcialmente cubierta. Solo se probó un camino.

---

### Actividad 3 — Analizar las Líneas No Cubiertas

Busca las líneas rojas en tu reporte. Probablemente encontrarás cosas como:

```java
// En LibroService.java — esta línea aparece en ROJO
public void descatalogarLibro(Long id) {
    Libro libro = libroRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Libro no encontrado")); // ← ROJO
    libro.softDelete();
    libroRepository.save(libro);
}
```

Si la línea del `orElseThrow` está en rojo, significa que **nunca probaste el caso donde el libro no existe**. Escribe un test para cubrirla:

```java
// En LibroServiceTest.java
@Test
@DisplayName("descatalogarLibro() debería lanzar excepción cuando el libro no existe")
void descatalogarLibroInexistenteDeberiaLanzarExcepcion() {
    // GIVEN
    when(libroRepository.findById(999L)).thenReturn(Optional.empty());

    // WHEN + THEN
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        libroService.descatalogarLibro(999L);
    });

    assertEquals("Libro no encontrado", exception.getMessage());
}
```

---

### Actividad 4 — Definir un Umbral Mínimo de Cobertura (Opcional)

Puedes configurar JaCoCo para que **falle el build** si la cobertura cae por debajo de un porcentaje mínimo:

```xml
<!-- Agregar DESPUÉS de la ejecución "report" en el pom.xml -->
<execution>
    <id>check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum> <!-- 60% mínimo -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

Con esta configuración, si ejecutas `mvn verify` y la cobertura es menor al 60%, Maven mostrará un error y el build fallará.

> 💡 **¿Cuánta cobertura es suficiente?** No hay una respuesta universal. En la industria:
> - **60-70%** se considera aceptable para la mayoría de proyectos.
> - **80%+** es un buen objetivo para servicios críticos.
> - **100%** casi nunca vale la pena perseguir — el esfuerzo extra no compensa.

---

### Actividad 5 — Buenas Prácticas de Testing

Ahora que has escrito tests en 5 fases, repasemos las reglas de oro:

#### 🏆 Regla 1: Un test debe probar UNA sola cosa

```java
// ❌ MAL — Este test prueba demasiadas cosas
@Test
void testGeneral() {
    Libro libro = service.guardar(new Libro(...));
    assertNotNull(libro.getId());
    List<Libro> todos = service.obtenerTodos();
    assertEquals(1, todos.size());
    service.eliminar(libro.getId());
    assertTrue(service.obtenerTodos().isEmpty());
}

// ✅ BIEN — Cada test prueba una operación
@Test void guardarDeberiaAsignarId() { ... }
@Test void obtenerTodosDeberiaRetornarLista() { ... }
@Test void eliminarDeberiaRemoverElLibro() { ... }
```

#### 🏆 Regla 2: Los nombres de los tests deben describir el comportamiento

```java
// ❌ MAL
@Test void test1() { ... }
@Test void testLibro() { ... }

// ✅ BIEN
@Test void guardarLibroDeberiaAsignarIdAutoincremental() { ... }
@Test void buscarPorAutorDeberiaRetornarListaVaciaSiNoExiste() { ... }
```

#### 🏆 Regla 3: No pruebes implementaciones privadas

```java
// ❌ MAL — Probar métodos privados directamente
// (indica que tu test está acoplado a la implementación)

// ✅ BIEN — Prueba el comportamiento público
// El método público invoca los métodos privados internamente
```

#### 🏆 Regla 4: Los tests no deben depender de orden

```java
// ❌ MAL — El test 2 depende de que el test 1 ya se ejecutó
@Test @Order(1) void crearLibro() { ... }
@Test @Order(2) void verificarLibroCreado() { ... } // Falla si se ejecuta solo

// ✅ BIEN — Cada test crea sus propios datos
@Test void crearYVerificarLibro() {
    Libro creado = service.guardar(new Libro(...));
    Optional<Libro> encontrado = service.obtenerPorId(creado.getId());
    assertTrue(encontrado.isPresent());
}
```

> 💡 **Excepción**: En tests de integración (Fase 5) a veces SÍ usamos `@Order` para simular un flujo de negocio completo. Pero es la excepción, no la regla.

#### 🏆 Regla 5: Usa `@DisplayName` siempre

Cuando tengas 200 tests y uno falle, agradecerás tener nombres legibles:

```
❌ Tests run: 200, Failures: 1 → guardarTest()
✅ Tests run: 200, Failures: 1 → "Debería lanzar excepción al guardar libro con ISBN duplicado"
```

---

### Actividad 6 — Organizar la Suite de Tests

La estructura final de tu carpeta de tests debería verse así:

```
src/test/
├── java/com/librotech/
│   ├── model/
│   │   ├── LibroTest.java              ← Tests unitarios del modelo
│   │   └── EditorialTest.java          ← Tests unitarios de Editorial
│   ├── service/
│   │   └── LibroServiceTest.java       ← Tests unitarios con Mockito
│   ├── repository/
│   │   └── LibroRepositoryTest.java    ← Tests de BD con @DataJpaTest
│   ├── controller/
│   │   └── LibroControllerTest.java    ← Tests de API con MockMvc
│   └── integration/
│       └── LibroIntegrationTest.java   ← Tests de integración completa
│
└── resources/
    └── application-test.properties     ← Config de BD para tests
```

Ejecuta **toda** tu suite con:

```bash
mvn clean test
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| JaCoCo está configurado en el `pom.xml` | ☐ |
| Se genera el reporte HTML en `target/site/jacoco/` | ☐ |
| Se identificaron y cubrieron al menos 3 líneas rojas | ☐ |
| Los tests siguen las buenas prácticas (nombres descriptivos, un assert por concepto) | ☐ |
| La cobertura general del proyecto es de al menos 60% | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Es posible tener 100% de cobertura de código y aun así tener bugs? Explica cómo.
2. ¿Qué paquete/clase de tu proyecto tiene la cobertura más baja? ¿Por qué crees que es?
3. ¿Cuál es el costo de buscar el 100% de cobertura vs quedarse en el 70%? ¿Cuándo vale la pena?
4. ¿Cómo ayudan las buenas prácticas de testing a un equipo de desarrollo que crece de 2 a 10 personas?

---

## ➡️ Siguiente Fase

Para la última fase, avanza a **[Fase 7 — Automatización con Maven y Conceptos de CI/CD](./07-Fase7-Automatizacion-CI.md)**, donde aprenderás a integrar tus tests en un pipeline de despliegue continuo.
