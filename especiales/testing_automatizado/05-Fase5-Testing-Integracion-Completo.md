# ⚙️ FASE 5 — Testing de Integración Completo con @SpringBootTest

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Avanzado  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender la diferencia entre **tests aislados** (Fases 2-4) y **tests de integración completos**.
2. Usar `@SpringBootTest` para levantar toda la aplicación y probar el flujo real de extremo a extremo.
3. Usar `TestRestTemplate` para hacer peticiones HTTP reales contra el servidor de test.
4. Probar un **flujo completo de negocio**: crear un libro, buscarlo, actualizarlo y eliminarlo.
5. Decidir cuándo usar tests de integración y cuándo son suficientes los tests unitarios.

---

## 🧠 ¿Qué diferencia hay con las fases anteriores?

| Fase | Herramienta | ¿Qué carga? | ¿Base de datos? | ¿Servidor HTTP? |
|---|---|---|---|---|
| Fase 2 (Service) | `@Mock` / Mockito | Solo la clase a probar | ❌ No | ❌ No |
| Fase 3 (Repository) | `@DataJpaTest` | Solo capa de datos | ✅ H2 en memoria | ❌ No |
| Fase 4 (Controller) | `@WebMvcTest` | Solo capa web | ❌ No | ❌ Simulado |
| **Fase 5 (Integración)** | **`@SpringBootTest`** | **TODO** | **✅ H2 en memoria** | **✅ Real** |

### Analogía

- **Fases 2-4** = Probar cada pieza de un auto por separado: el motor, las ruedas, el volante.
- **Fase 5** = Subirse al auto y manejar de verdad. Aquí verificas que TODAS las piezas funcionan juntas.

---

## 📖 Contexto de Negocio

El director de tecnología de **LibroTech** quiere saber: "Si un usuario hace una petición real a nuestra API, ¿todo funciona de punta a punta?" Las pruebas de integración responden exactamente esa pregunta. Son las más cercanas a lo que hace un usuario real.

---

## 📝 Actividades

### Actividad 1 — Configurar el Test de Integración

Crea: `src/test/java/com/librotech/integration/LibroIntegrationTest.java`

```java
package com.librotech.integration;

import com.librotech.model.Libro;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibroIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate; // ← Cliente HTTP real para tests
}
```

#### 🔍 Explicación de cada anotación:

**`@SpringBootTest(webEnvironment = RANDOM_PORT)`**
```
Traduce a: "Levanta TODA la aplicación Spring Boot en un puerto aleatorio"
- Incluye: Controllers + Services + Repositories + Base de datos
- Usa un puerto aleatorio para evitar conflictos si otro test está corriendo
```

**`@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)`**
```
Traduce a: "Después de cada test, resetea el contexto de Spring"
- Esto asegura que cada test empiece con un estado limpio
- La base de datos se limpia entre tests
- Es más lento, pero más seguro
```

**`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`**
```
Traduce a: "Ejecuta los tests en el orden que yo defina con @Order"
- Normalmente los tests no tienen orden
- Pero en integración, a veces queremos probar un FLUJO secuencial
```

**`TestRestTemplate`**
```
Traduce a: "Un cliente HTTP (como Postman) pero dentro del test"
- Hace peticiones HTTP reales al servidor que levantó @SpringBootTest
- Las peticiones viajan por la red (localhost), igual que en producción
```

---

### Actividad 2 — Probar el Flujo Completo: Crear y Consultar

```java
@Test
@Order(1)
@DisplayName("Flujo completo: Crear un libro y luego consultarlo")
void crearYConsultarLibro() {
    // ======= PASO 1: CREAR un libro via POST =======
    Libro nuevoLibro = new Libro(null, "Cien años de soledad", 
        "García Márquez", "ISBN-INT-001", 1967);

    ResponseEntity<Libro> respuestaCrear = restTemplate.postForEntity(
        "/api/libros",    // URL del endpoint
        nuevoLibro,       // Body de la petición (se convierte a JSON automáticamente)
        Libro.class       // Tipo de la respuesta esperada
    );

    // Verificar que se creó correctamente
    assertEquals(HttpStatus.CREATED, respuestaCrear.getStatusCode(),
        "La creación debería retornar 201 CREATED");
    assertNotNull(respuestaCrear.getBody(), 
        "La respuesta debería contener el libro creado");
    assertNotNull(respuestaCrear.getBody().getId(),
        "El libro creado debería tener un ID asignado");

    Long idCreado = respuestaCrear.getBody().getId();

    // ======= PASO 2: CONSULTAR el libro que acabamos de crear =======
    ResponseEntity<Libro> respuestaConsultar = restTemplate.getForEntity(
        "/api/libros/" + idCreado,
        Libro.class
    );

    assertEquals(HttpStatus.OK, respuestaConsultar.getStatusCode());
    assertEquals("Cien años de soledad", respuestaConsultar.getBody().getTitulo(),
        "El libro consultado debería ser el mismo que creamos");
}
```

#### 🔍 ¿Qué es `restTemplate.postForEntity()`?

Es el equivalente a hacer esto en Postman:
1. Seleccionar método POST
2. Escribir la URL `/api/libros`
3. Pegar el JSON en el body
4. Dar clic en "Send"
5. Leer la respuesta

Pero todo en una sola línea de código.

---

### Actividad 3 — Probar el Flujo de Actualización

```java
@Test
@Order(2)
@DisplayName("Flujo completo: Crear un libro, actualizarlo y verificar los cambios")
void crearYActualizarLibro() {
    // PASO 1: Crear
    Libro original = new Libro(null, "Titulo Original", "Autor Original", "ISBN-UPD-001", 2020);
    ResponseEntity<Libro> creado = restTemplate.postForEntity("/api/libros", original, Libro.class);
    Long id = creado.getBody().getId();

    // PASO 2: Actualizar
    Libro datosNuevos = new Libro(null, "Titulo Modificado", "Autor Modificado", "ISBN-UPD-002", 2024);

    // Para PUT, usamos exchange() porque restTemplate no tiene un putForEntity() directo
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Libro> request = new HttpEntity<>(datosNuevos, headers);

    ResponseEntity<Libro> respuestaActualizar = restTemplate.exchange(
        "/api/libros/" + id,
        HttpMethod.PUT,
        request,
        Libro.class
    );

    // PASO 3: Verificar
    assertEquals(HttpStatus.OK, respuestaActualizar.getStatusCode());
    assertEquals("Titulo Modificado", respuestaActualizar.getBody().getTitulo(),
        "El título debería haberse actualizado");
    assertEquals("Autor Modificado", respuestaActualizar.getBody().getAutor(),
        "El autor debería haberse actualizado");
}
```

---

### Actividad 4 — Probar el Flujo de Eliminación

```java
@Test
@Order(3)
@DisplayName("Flujo completo: Crear un libro, eliminarlo y verificar que ya no existe")
void crearEliminarYVerificar() {
    // PASO 1: Crear
    Libro libro = new Libro(null, "Libro a Eliminar", "Autor", "ISBN-DEL-001", 2024);
    ResponseEntity<Libro> creado = restTemplate.postForEntity("/api/libros", libro, Libro.class);
    Long id = creado.getBody().getId();

    // PASO 2: Eliminar
    restTemplate.delete("/api/libros/" + id);

    // PASO 3: Verificar que ya no existe
    ResponseEntity<String> respuesta = restTemplate.getForEntity(
        "/api/libros/" + id, String.class);

    assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode(),
        "Un libro eliminado debería retornar 404 al consultarlo");
}
```

---

### Actividad 5 — Probar Casos de Error en Integración

```java
@Test
@Order(4)
@DisplayName("GET a un ID inexistente debería retornar 404")
void consultarLibroInexistente() {
    ResponseEntity<String> respuesta = restTemplate.getForEntity(
        "/api/libros/99999", String.class);

    assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
}

@Test
@Order(5)
@DisplayName("Crear múltiples libros y listar todos")
void crearMultiplesYListar() {
    // Crear 3 libros
    restTemplate.postForEntity("/api/libros",
        new Libro(null, "Libro 1", "Autor 1", "ISBN-M01", 2020), Libro.class);
    restTemplate.postForEntity("/api/libros",
        new Libro(null, "Libro 2", "Autor 2", "ISBN-M02", 2021), Libro.class);
    restTemplate.postForEntity("/api/libros",
        new Libro(null, "Libro 3", "Autor 3", "ISBN-M03", 2022), Libro.class);

    // Listar todos
    ResponseEntity<Libro[]> respuesta = restTemplate.getForEntity(
        "/api/libros", Libro[].class);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertTrue(respuesta.getBody().length >= 3,
        "Deberían existir al menos los 3 libros que creamos");
}
```

> ⚠️ **Nota sobre paginación**: Si tu endpoint `GET /api/libros` retorna un `Page<Libro>` en vez de una `List<Libro>`, necesitarás adaptar la deserialización. La forma más simple es recibir la respuesta como `String` y parsear el JSON manualmente con `ObjectMapper`.

---

### Actividad 6 — ¿Cuándo usar cada tipo de test?

Aquí tienes una guía de decisión que resume las 5 fases:

```
¿Qué quiero probar?
│
├─ ¿La lógica de UN método sin dependencias?
│   → Fase 1: JUnit básico (sin mocks)
│
├─ ¿La lógica de negocio del Service?
│   → Fase 2: Mockito (@Mock + @InjectMocks)
│
├─ ¿Que mis consultas SQL funcionan?
│   → Fase 3: @DataJpaTest
│
├─ ¿Que mi endpoint responde con el JSON y código HTTP correcto?
│   → Fase 4: @WebMvcTest + MockMvc
│
└─ ¿Que TODO funciona de punta a punta?
    → Fase 5: @SpringBootTest + TestRestTemplate
```

**Regla de oro**: Escribe MUCHOS tests unitarios (Fases 1-2), ALGUNOS tests de repositorio y controlador (Fases 3-4), y POCOS tests de integración completa (Fase 5).

---

### Actividad 7 — Práctica Autónoma

1. **Test de flujo de negocio**: Escribe un test que cree un libro, lo actualice, lo consulte y finalmente lo elimine — todo en un solo test secuencial.

2. **Test con datos relacionados** (si tienes el modelo de Semana 4): Crea primero una Editorial, luego un Libro asociado a esa editorial, y verifica que al consultar el libro, la editorial viene incluida.

3. **Test de soft delete** (si lo implementaste): Crea un libro, "descatalógalo" (soft delete), y verifica que ya no aparece en `GET /api/libros` pero que sigue existiendo en la base de datos.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se usa `@SpringBootTest` con `RANDOM_PORT` | ☐ |
| Se usa `TestRestTemplate` para peticiones HTTP reales | ☐ |
| Se prueba un flujo completo: Crear → Consultar → Verificar | ☐ |
| Se prueba el flujo de eliminación y verificación de 404 | ☐ |
| Se prueban al menos 3 escenarios de integración | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué los tests de integración son más lentos que los unitarios? ¿Deberíamos tener más tests de integración que unitarios?
2. ¿Qué pasa si un test de integración falla? ¿Cómo sabrías si el problema está en el controlador, el servicio o el repositorio?
3. ¿Por qué usamos `RANDOM_PORT` en vez de un puerto fijo como `8080`?
4. ¿Qué ventaja tiene `@DirtiesContext` y cuál es su costo en tiempo de ejecución?

---

## ➡️ Siguiente Fase

Ahora que tienes tests en todas las capas, avanza a **[Fase 6 — Cobertura de Código y Buenas Prácticas](./06-Fase6-Cobertura-Buenas-Practicas.md)**, donde aprenderás a medir qué porcentaje de tu código está cubierto por tests.
