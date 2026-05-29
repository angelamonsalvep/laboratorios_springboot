# ⚙️ FASE 1 — Fundamentos de JUnit 5: Tu Primera Prueba Automatizada

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 45 minutos  
**Nivel:** Principiante  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender **qué es JUnit 5** y por qué es la herramienta estándar de testing en Java.
2. Escribir tu **primera prueba automatizada** — desde cero.
3. Dominar las **aserciones básicas** (`assertEquals`, `assertNotNull`, `assertTrue`, etc.).
4. Aplicar el patrón **Given-When-Then** para estructurar tus pruebas de forma clara.
5. Ejecutar tus tests desde el IDE y desde la terminal con Maven.

---

## 🧠 ¿Qué es JUnit 5 y por qué me importa?

**JUnit 5** es un framework (una herramienta) que te permite escribir "mini-programas" que prueban tu código automáticamente. En vez de abrir Postman cada vez que haces un cambio para verificar que todo sigue funcionando, escribes una prueba una sola vez y la ejecutas cientos de veces con un solo clic.

### Analogía Simple

Imagina que eres chef en el restaurante **LibroTech Café**:

- **Sin tests:** Cada vez que cambias la receta del café, le pides a alguien que lo pruebe. Si el probador está enfermo, nadie sabe si el café sabe bien.
- **Con tests:** Tienes una máquina que mide automáticamente el sabor, la temperatura y la cantidad de azúcar. Si cambias algo, la máquina te dice inmediatamente "ALERTA: el café está muy amargo".

JUnit 5 es esa máquina para tu código.

### Las 3 Anotaciones Esenciales

| Anotación | ¿Qué hace? | Ejemplo mental |
|---|---|---|
| `@Test` | Le dice a JUnit "esto es una prueba, ejecútala" | Es como poner una etiqueta que dice "esto se debe revisar" |
| `@DisplayName("...")` | Le da un nombre bonito y legible a la prueba | En vez de ver `testGuardarLibro()`, ves "Debería guardar un libro correctamente" |
| `@BeforeEach` | Ejecuta código ANTES de cada prueba (para preparar datos) | Es como preparar los ingredientes antes de cocinar |

---

## 📖 Contexto de Negocio — ¿Qué vamos a probar?

El equipo de calidad de **LibroTech** ha decidido que antes de seguir desarrollando nuevas funcionalidades, necesitamos una **red de seguridad** para el código existente. Vamos a empezar por lo más básico: verificar que la entidad `Libro` funciona correctamente — que sus getters y setters hacen lo que deben hacer, y que sus constructores crean objetos con los valores correctos.

> 🤔 **"¿Probar getters y setters no es demasiado simple?"**
> 
> ¡Sí, es simple! Y ese es exactamente el punto. Empezamos con algo fácil para que te sientas cómodo con la herramienta. Es como aprender a manejar en un parqueadero vacío antes de salir a la autopista.

---

## 📝 Actividades

### Actividad 1 — Tu Primera Prueba: Verificar el Constructor de `Libro`

Recuerda que tu entidad `Libro` (creada en la Semana 2) tiene estos campos principales: `id`, `titulo`, `autor`, `isbn`, `anioPublicacion`.

Crea el archivo de test en: `src/test/java/com/librotech/model/LibroTest.java`

```java
package com.librotech.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    @Test
    @DisplayName("Debería crear un libro con todos sus campos correctamente")
    void crearLibroConConstructor() {
        // ========== GIVEN (Dado) ==========
        // Preparamos los datos que necesitamos
        Long id = 1L;
        String titulo = "Cien años de soledad";
        String autor = "Gabriel García Márquez";
        String isbn = "978-0307474728";
        int anio = 1967;

        // ========== WHEN (Cuando) ==========
        // Ejecutamos la acción que queremos probar
        Libro libro = new Libro(id, titulo, autor, isbn, anio);

        // ========== THEN (Entonces) ==========
        // Verificamos que el resultado es el esperado
        assertEquals(id, libro.getId());
        assertEquals(titulo, libro.getTitulo());
        assertEquals(autor, libro.getAutor());
        assertEquals(isbn, libro.getIsbn());
        assertEquals(anio, libro.getAnioPublicacion());
    }
}
```

#### 🔍 Explicación línea por línea:

1. **`class LibroTest`** — La convención es nombrar la clase de test igual que la clase que prueba + `Test` al final. Como probamos `Libro`, la llamamos `LibroTest`.

2. **`@Test`** — Sin esta anotación, JUnit no sabe que este método es una prueba. Sin ella, es solo un método normal que nunca se ejecuta.

3. **`@DisplayName("...")`** — Cuando ejecutes los tests, en vez de ver el nombre del método (`crearLibroConConstructor`), verás el texto descriptivo. Esto es útil cuando tienes 50 tests y necesitas encontrar cuál falló.

4. **`assertEquals(esperado, actual)`** — Esta es la **aserción** más común. Dice: "Espero que el valor `esperado` sea **exactamente igual** al valor `actual`. Si no son iguales, la prueba FALLA."

---

### Actividad 2 — Ejecutar la prueba

#### Opción A: Desde el IDE (IntelliJ IDEA)
1. Haz clic derecho sobre la clase `LibroTest`.
2. Selecciona **"Run LibroTest"**.
3. Deberías ver una **barra verde** con ✅ — significa que la prueba pasó.

#### Opción B: Desde la terminal con Maven
```bash
mvn test -Dtest=LibroTest
```

Deberías ver algo como:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> 🎯 **¿Qué significa cada número?**
> - `Tests run: 1` → Se ejecutó 1 prueba.
> - `Failures: 0` → Ninguna prueba falló (¡bien!).
> - `Errors: 0` → No hubo errores técnicos.
> - `Skipped: 0` → No se saltó ninguna prueba.

---

### Actividad 3 — Más Pruebas: Explorando Diferentes Aserciones

Agrega estos tests a tu clase `LibroTest`:

```java
@Test
@DisplayName("El constructor vacío debería crear un libro con campos nulos")
void crearLibroVacio() {
    // GIVEN + WHEN
    Libro libro = new Libro();

    // THEN
    assertNull(libro.getId(), "El ID debería ser nulo al crear con constructor vacío");
    assertNull(libro.getTitulo(), "El título debería ser nulo");
    assertNull(libro.getAutor(), "El autor debería ser nulo");
}

@Test
@DisplayName("Los setters deberían modificar los valores correctamente")
void probarSetters() {
    // GIVEN
    Libro libro = new Libro();

    // WHEN
    libro.setTitulo("El Principito");
    libro.setAutor("Antoine de Saint-Exupéry");
    libro.setAnioPublicacion(1943);

    // THEN
    assertEquals("El Principito", libro.getTitulo());
    assertEquals("Antoine de Saint-Exupéry", libro.getAutor());
    assertEquals(1943, libro.getAnioPublicacion());
}

@Test
@DisplayName("Dos libros con el mismo ISBN no deberían ser el mismo objeto")
void librosConMismoIsbnSonObjetosDiferentes() {
    // GIVEN
    Libro libro1 = new Libro(1L, "Libro A", "Autor", "ISBN-123", 2020);
    Libro libro2 = new Libro(2L, "Libro B", "Otro Autor", "ISBN-123", 2021);

    // WHEN + THEN
    assertNotEquals(libro1, libro2, 
        "Aunque tengan el mismo ISBN, son objetos distintos");
    assertEquals(libro1.getIsbn(), libro2.getIsbn(), 
        "Pero sus ISBNs sí deberían ser iguales");
}
```

#### 📊 Tabla de Aserciones que acabas de usar:

| Aserción | ¿Qué verifica? | Ejemplo |
|---|---|---|
| `assertEquals(a, b)` | Que `a` y `b` sean iguales | `assertEquals("Hola", saludo)` |
| `assertNotEquals(a, b)` | Que `a` y `b` NO sean iguales | `assertNotEquals(libro1, libro2)` |
| `assertNull(x)` | Que `x` sea `null` | `assertNull(libro.getId())` |
| `assertNotNull(x)` | Que `x` NO sea `null` | `assertNotNull(libro.getTitulo())` |
| `assertTrue(condicion)` | Que la condición sea `true` | `assertTrue(lista.isEmpty())` |
| `assertFalse(condicion)` | Que la condición sea `false` | `assertFalse(lista.isEmpty())` |

> 💡 **Tip**: El último parámetro `String` en las aserciones es el **mensaje de error**. Es opcional, pero altamente recomendado porque te dice POR QUÉ falló la prueba cuando algo sale mal.

---

### Actividad 4 — Preparar datos repetidos con `@BeforeEach`

Cuando tienes muchas pruebas que necesitan el mismo libro de ejemplo, puedes usar `@BeforeEach` para evitar repetir código:

```java
package com.librotech.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    // Variable compartida entre los tests
    private Libro libroBase;

    @BeforeEach
    void setUp() {
        // Este método se ejecuta ANTES de CADA test
        // Así cada test empieza con un libro "fresco"
        libroBase = new Libro(1L, "Cien años de soledad", 
            "Gabriel García Márquez", "978-0307474728", 1967);
    }

    @Test
    @DisplayName("Debería crear un libro con todos sus campos correctamente")
    void crearLibroConConstructor() {
        // Ahora usamos libroBase directamente
        assertEquals("Cien años de soledad", libroBase.getTitulo());
        assertEquals("Gabriel García Márquez", libroBase.getAutor());
    }

    @Test
    @DisplayName("Modificar un libro NO debería afectar a los otros tests")
    void modificarLibroNoAfectaOtrosTests() {
        // WHEN - Modificamos el libro
        libroBase.setTitulo("Título Cambiado");

        // THEN - El cambio solo aplica a ESTE test
        assertEquals("Título Cambiado", libroBase.getTitulo());
        // En el test anterior, libroBase seguirá teniendo 
        // "Cien años de soledad" porque @BeforeEach lo reinicia
    }

    // ... (tus tests anteriores siguen aquí)
}
```

> 🔑 **Concepto clave**: `@BeforeEach` garantiza que cada prueba es **independiente**. Lo que hagas en un test NO afecta a otro. Esto se llama **aislamiento de tests**.

---

### Actividad 5 — Práctica Autónoma: Probar la Entidad `Editorial`

Ahora es tu turno. Crea `src/test/java/com/librotech/model/EditorialTest.java` y escribe:

1. **Un test** que verifique que el constructor de `Editorial` asigna correctamente `nombre`, `direccion`, `pais` y `fundadoEn`.
2. **Un test** que verifique que el constructor vacío crea una editorial con `id = null`.
3. **Un test** que verifique que los setters funcionan correctamente.

> 💡 **Pista**: Sigue exactamente el mismo patrón Given-When-Then que usaste para `LibroTest`.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se creó la clase `LibroTest` en la ruta correcta (`src/test/java/...`) | ☐ |
| Se usan las anotaciones `@Test` y `@DisplayName` en cada prueba | ☐ |
| Se aplica el patrón Given-When-Then de forma clara | ☐ |
| Se utilizan al menos 3 tipos diferentes de aserciones | ☐ |
| Se usa `@BeforeEach` para preparar datos comunes | ☐ |
| Todos los tests pasan con barra verde (o `BUILD SUCCESS` en Maven) | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si cambias `assertEquals("Cien años de soledad", ...)` a `assertEquals("cien años de soledad", ...)`? ¿La prueba pasaría o fallaría? ¿Por qué?
2. ¿Cuál es la diferencia entre `assertNull` y `assertEquals(null, valor)`? ¿El resultado es el mismo?
3. Si eliminas `@BeforeEach` y creas el `libroBase` directamente como atributo `private Libro libroBase = new Libro(...)`, ¿funcionaría igual? ¿Qué problema podría surgir?
4. ¿Por qué es importante que cada test sea independiente de los demás?

---

## ➡️ Siguiente Fase

Una vez domines los fundamentos de JUnit, avanza a **[Fase 2 — Testing Unitario del Servicio con Mockito](./02-Fase2-Testing-Unitario-Servicio.md)**, donde aprenderás a probar la lógica de negocio **sin necesitar una base de datos**.
