# ⚙️ FASE 4 — Testing del Controlador con MockMvc

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Intermedio-Avanzado  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender qué es **MockMvc** y por qué lo usamos para probar APIs REST.
2. Simular peticiones **GET, POST, PUT y DELETE** sin levantar un servidor real.
3. Verificar **códigos de estado HTTP** (200, 201, 204, 404) en las respuestas.
4. Verificar el **contenido JSON** de las respuestas.
5. Usar `@WebMvcTest` para cargar SOLO la capa de controlador.

---

## 🧠 ¿Qué es MockMvc?

**MockMvc** es una herramienta de Spring que simula peticiones HTTP dentro de tu aplicación. Es como tener un **Postman automático** integrado en tus tests.

### ¿Por qué no usar Postman directamente?

| Postman | MockMvc |
|---|---|
| Necesitas tener la app corriendo | No necesitas levantar la app |
| Las pruebas son manuales | Las pruebas son automáticas |
| Si cambias algo, debes re-probar manualmente | Si cambias algo, los tests se re-ejecutan solos |
| No se integra con tu pipeline de CI/CD | Se ejecuta con `mvn test` automáticamente |

### Analogía

Imagina que quieres probar que tu buzón de correo funciona bien:

- **Postman** = Enviar una carta real por correo y esperar a que llegue.
- **MockMvc** = Simular el envío de la carta dentro de la oficina postal para verificar que el sistema de clasificación funciona. No necesitas esperar días.

---

## 📖 Contexto de Negocio

Los endpoints REST de **LibroTech** son la puerta de entrada al sistema. Si un endpoint devuelve un código 500 en vez de 404 cuando no encuentra un libro, los clientes (apps móviles, frontend web) no sabrán cómo manejar el error. Vamos a garantizar que cada endpoint responde con el **código HTTP correcto** y el **JSON esperado**.

---

## 🧠 ¿Qué hace `@WebMvcTest`?

Cuando pones `@WebMvcTest(LibroController.class)` en tu test:

```
Lo que @WebMvcTest carga:
  ✅ El controlador especificado (LibroController)
  ✅ MockMvc (para simular peticiones HTTP)
  ✅ Los filtros y configuración web de Spring
  ❌ Servicios (@Service) — debes usar @MockBean
  ❌ Repositorios (@Repository)
  ❌ Base de datos
```

Es como armar un escenario de teatro donde solo está el actor principal (el controller) y todo lo demás son decorados de cartón (mocks).

---

## 📝 Actividades

### Actividad 1 — Configurar el Test del Controlador

Crea: `src/test/java/com/librotech/controller/LibroControllerTest.java`

```java
package com.librotech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librotech.model.Libro;
import com.librotech.service.LibroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibroController.class) // ← Carga SOLO este controlador
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc; // ← El "Postman automático"

    @MockBean // ← Crea un mock del servicio e lo inyecta en el controlador
    private LibroService libroService;

    @Autowired
    private ObjectMapper objectMapper; // ← Convierte objetos Java ↔ JSON
}
```

#### 🔍 ¿Cuál es la diferencia entre `@Mock` y `@MockBean`?

| Anotación | Framework | ¿Dónde vive el mock? |
|---|---|---|
| `@Mock` | Mockito puro | Solo en tu test — NO está en el contexto de Spring |
| `@MockBean` | Spring Boot Test | Se registra en el contexto de Spring — el Controller lo recibe por `@Autowired` |

Usamos `@MockBean` porque el controlador obtiene su `LibroService` mediante inyección de dependencias de Spring. Si usáramos solo `@Mock`, Spring no sabría que debe usar nuestro mock.

---

### Actividad 2 — Probar GET `/api/libros` (Listar todos)

```java
@Test
@DisplayName("GET /api/libros debería retornar 200 y la lista de libros en JSON")
void listarLibrosDeberiaRetornar200ConListaJson() throws Exception {
    // GIVEN
    Libro libro1 = new Libro(1L, "Cien años de soledad", "García Márquez", "ISBN-001", 1967);
    Libro libro2 = new Libro(2L, "1984", "George Orwell", "ISBN-002", 1949);
    when(libroService.obtenerTodos()).thenReturn(Arrays.asList(libro1, libro2));

    // WHEN + THEN
    mockMvc.perform(get("/api/libros"))               // Simula: GET /api/libros
        .andExpect(status().isOk())                    // Verifica: código 200
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Verifica: respuesta es JSON
        .andExpect(jsonPath("$", hasSize(2)))           // Verifica: el array tiene 2 elementos
        .andExpect(jsonPath("$[0].titulo", is("Cien años de soledad")))  // Verifica: primer título
        .andExpect(jsonPath("$[1].titulo", is("1984")));                  // Verifica: segundo título
}
```

#### 🔍 Desglose línea por línea:

```java
mockMvc.perform(get("/api/libros"))
// Traduce a: "Haz una petición GET a la ruta /api/libros"

.andExpect(status().isOk())
// Traduce a: "Espero que el código de respuesta sea 200 OK"

.andExpect(jsonPath("$", hasSize(2)))
// Traduce a: "En el JSON de respuesta, el array raíz ($) tiene 2 elementos"

.andExpect(jsonPath("$[0].titulo", is("Cien años de soledad")))
// Traduce a: "El primer elemento ($[0]) tiene un campo 'titulo' con ese valor"
```

**Guía rápida de `jsonPath`:**

| Expresión | Significado |
|---|---|
| `$` | El elemento raíz del JSON |
| `$[0]` | El primer elemento del array |
| `$[0].titulo` | El campo "titulo" del primer elemento |
| `$.length()` | El tamaño del array |

---

### Actividad 3 — Probar GET `/api/libros/{id}` (Buscar por ID)

```java
@Test
@DisplayName("GET /api/libros/1 debería retornar 200 y el libro encontrado")
void obtenerPorIdExistenteDeberiaRetornar200() throws Exception {
    // GIVEN
    Libro libro = new Libro(1L, "El Principito", "Saint-Exupéry", "ISBN-EP", 1943);
    when(libroService.obtenerPorId(1L)).thenReturn(Optional.of(libro));

    // WHEN + THEN
    mockMvc.perform(get("/api/libros/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo", is("El Principito")))
        .andExpect(jsonPath("$.autor", is("Saint-Exupéry")))
        .andExpect(jsonPath("$.id", is(1)));
}

@Test
@DisplayName("GET /api/libros/999 debería retornar 404 cuando el libro no existe")
void obtenerPorIdInexistenteDeberiaRetornar404() throws Exception {
    // GIVEN
    when(libroService.obtenerPorId(999L)).thenReturn(Optional.empty());

    // WHEN + THEN
    mockMvc.perform(get("/api/libros/999"))
        .andExpect(status().isNotFound());  // Verifica: código 404
}
```

> 🔑 **Punto clave**: Estamos probando que el **controlador** maneja correctamente el `Optional.empty()` del servicio y devuelve 404. Si tu controlador tiene un bug (por ejemplo, retorna 200 con body null), este test lo detectará.

---

### Actividad 4 — Probar POST `/api/libros` (Crear libro)

```java
@Test
@DisplayName("POST /api/libros debería retornar 201 y el libro creado con ID")
void crearLibroDeberiaRetornar201() throws Exception {
    // GIVEN
    Libro libroSinId = new Libro(null, "Nuevo Libro", "Autor Nuevo", "ISBN-NEW", 2024);
    Libro libroConId = new Libro(1L, "Nuevo Libro", "Autor Nuevo", "ISBN-NEW", 2024);
    when(libroService.guardar(any(Libro.class))).thenReturn(libroConId);

    // WHEN + THEN
    mockMvc.perform(post("/api/libros")
            .contentType(MediaType.APPLICATION_JSON)          // Enviamos JSON
            .content(objectMapper.writeValueAsString(libroSinId)))  // Convertimos el objeto a JSON
        .andExpect(status().isCreated())                      // Verifica: código 201
        .andExpect(jsonPath("$.id", is(1)))                   // Verifica: tiene ID asignado
        .andExpect(jsonPath("$.titulo", is("Nuevo Libro")));  // Verifica: título correcto
}
```

#### 🔍 ¿Qué es `objectMapper.writeValueAsString()`?

El `ObjectMapper` de Jackson convierte un objeto Java a su representación JSON en texto:

```java
Libro libro = new Libro(null, "Test", "Autor", "ISBN", 2024);
String json = objectMapper.writeValueAsString(libro);
// json = {"id":null,"titulo":"Test","autor":"Autor","isbn":"ISBN","anioPublicacion":2024}
```

Es necesario porque `mockMvc.perform(post(...).content(...))` espera recibir el body como un `String` en formato JSON.

---

### Actividad 5 — Probar PUT `/api/libros/{id}` (Actualizar)

```java
@Test
@DisplayName("PUT /api/libros/1 debería retornar 200 con los datos actualizados")
void actualizarLibroExistenteDeberiaRetornar200() throws Exception {
    // GIVEN
    Libro datosNuevos = new Libro(null, "Título Actualizado", "Autor Actualizado", "ISBN-UPD", 2024);
    Libro libroActualizado = new Libro(1L, "Título Actualizado", "Autor Actualizado", "ISBN-UPD", 2024);
    when(libroService.actualizar(eq(1L), any(Libro.class))).thenReturn(Optional.of(libroActualizado));

    // WHEN + THEN
    mockMvc.perform(put("/api/libros/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(datosNuevos)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo", is("Título Actualizado")));
}

@Test
@DisplayName("PUT /api/libros/999 debería retornar 404 si el libro no existe")
void actualizarLibroInexistenteDeberiaRetornar404() throws Exception {
    // GIVEN
    Libro datosNuevos = new Libro(null, "Datos", "Autor", "ISBN", 2024);
    when(libroService.actualizar(eq(999L), any(Libro.class))).thenReturn(Optional.empty());

    // WHEN + THEN
    mockMvc.perform(put("/api/libros/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(datosNuevos)))
        .andExpect(status().isNotFound());
}
```

#### 🔍 ¿Qué es `eq()` vs `any()`?

```java
when(libroService.actualizar(eq(1L), any(Libro.class)))
// eq(1L) → El primer argumento debe ser EXACTAMENTE 1L
// any(Libro.class) → El segundo argumento puede ser CUALQUIER Libro
```

Usamos `eq(1L)` porque nos importa que el controlador pase el ID correcto al servicio. Usamos `any()` para el cuerpo porque el objeto exacto se construye dentro de la petición.

---

### Actividad 6 — Probar DELETE `/api/libros/{id}`

```java
@Test
@DisplayName("DELETE /api/libros/1 debería retornar 204 cuando se elimina exitosamente")
void eliminarLibroExistenteDeberiaRetornar204() throws Exception {
    // GIVEN
    when(libroService.eliminar(1L)).thenReturn(true);

    // WHEN + THEN
    mockMvc.perform(delete("/api/libros/1"))
        .andExpect(status().isNoContent());  // 204 No Content
}

@Test
@DisplayName("DELETE /api/libros/999 debería retornar 404 cuando el libro no existe")
void eliminarLibroInexistenteDeberiaRetornar404() throws Exception {
    // GIVEN
    when(libroService.eliminar(999L)).thenReturn(false);

    // WHEN + THEN
    mockMvc.perform(delete("/api/libros/999"))
        .andExpect(status().isNotFound());  // 404 Not Found
}
```

---

### Actividad 7 — Resumen Visual de Todos los Tests

Aquí tienes la tabla completa de lo que has cubierto:

| Endpoint | Caso | Código esperado | ¿Probado? |
|---|---|---|---|
| `GET /api/libros` | Hay libros | 200 + lista JSON | ✅ |
| `GET /api/libros/{id}` | Libro existe | 200 + libro JSON | ✅ |
| `GET /api/libros/{id}` | Libro NO existe | 404 | ✅ |
| `POST /api/libros` | Datos válidos | 201 + libro con ID | ✅ |
| `PUT /api/libros/{id}` | Libro existe | 200 + libro actualizado | ✅ |
| `PUT /api/libros/{id}` | Libro NO existe | 404 | ✅ |
| `DELETE /api/libros/{id}` | Libro existe | 204 (sin cuerpo) | ✅ |
| `DELETE /api/libros/{id}` | Libro NO existe | 404 | ✅ |

---

### Actividad 8 — Práctica Autónoma

1. **Probar GET `/api/libros` cuando no hay libros**: El servicio devuelve una lista vacía. ¿Qué código HTTP debería retornar? ¿200 con array vacío `[]`?

2. **Probar POST con JSON inválido**: Envía un body vacío `{}` o con campos faltantes. ¿Qué pasa? ¿El controller maneja ese caso?

3. **Crear `EditorialControllerTest`**: Aplica el mismo patrón para probar el CRUD de editoriales.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se usa `@WebMvcTest` para aislar el controlador | ☐ |
| El servicio está anotado con `@MockBean` | ☐ |
| Se prueban los 4 métodos HTTP: GET, POST, PUT, DELETE | ☐ |
| Se verifican los códigos de estado HTTP correctos | ☐ |
| Se verifica el contenido JSON de las respuestas con `jsonPath` | ☐ |
| Se prueban tanto casos exitosos como de error (404) | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué ventaja tiene usar `@WebMvcTest` sobre `@SpringBootTest` para probar controladores?
2. Si tu controlador usa `@Valid` para validar los datos de entrada, ¿MockMvc también ejecutaría esas validaciones?
3. ¿Qué pasaría si tu controlador devuelve `ResponseEntity.ok()` en vez de `ResponseEntity.status(NOT_FOUND)` cuando el libro no existe? ¿El test lo detectaría?
4. ¿Por qué usamos `ObjectMapper` para convertir objetos a JSON en vez de escribir el JSON como String manualmente?

---

## ➡️ Siguiente Fase

Ahora que probaste cada capa por separado, avanza a **[Fase 5 — Testing de Integración Completo](./05-Fase5-Testing-Integracion-Completo.md)**, donde conectarás TODAS las capas para una prueba de extremo a extremo.
