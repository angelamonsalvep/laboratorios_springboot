# ⚙️ FASE 2 — Testing Unitario del Servicio con Mockito

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 1.5 horas  
**Nivel:** Intermedio  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender **qué es un Mock** y por qué es necesario para probar servicios.
2. Usar **Mockito** para simular el comportamiento del repositorio.
3. Probar la lógica de negocio del `LibroService` **sin necesitar una base de datos**.
4. Usar `@Mock`, `@InjectMocks`, `when()` y `verify()`.
5. Probar tanto los **caminos felices** (happy paths) como los **caminos de error**.

---

## 🧠 ¿Qué es un Mock y por qué lo necesitamos?

### El Problema

Quieres probar tu `LibroService`, pero este depende del `LibroRepository`. Y el repositorio depende de una base de datos. Si para probar una sola función del servicio necesitas levantar la base de datos, configurar conexiones, sembrar datos... cada prueba tardaría segundos o minutos. Con 100 pruebas, te tomaría una eternidad.

### La Solución: Mocks (Objetos Simulados)

Un **Mock** es un objeto "falso" que simula el comportamiento de un objeto real. Es como un actor que interpreta el papel del repositorio:

```
SIN Mock:
  LibroService → LibroRepository → Base de Datos H2 (real)
  ⏱️ Lento. Necesitas BD configurada.

CON Mock:
  LibroService → LibroRepository (SIMULADO por Mockito)
  ⚡ Rápido. No necesitas BD.
```

### Analogía

Imagina que quieres probar que un cajero de banco procesa depósitos correctamente. 

- **Sin Mock:** Necesitas un banco real, una bóveda real, billetes reales.
- **Con Mock:** Le das al cajero billetes de juguete y una caja registradora de mentiras. Tú solo quieres saber si el cajero **sigue el procedimiento correctamente**, no si el banco funciona.

Mockito es la fábrica de "billetes de juguete" para tus tests.

---

## 📖 Contexto de Negocio — ¿Qué vamos a probar?

El `LibroService` es el **cerebro** de LibroTech. Contiene todas las reglas de negocio: 
- ¿Qué pasa cuando guardas un libro?
- ¿Qué pasa cuando buscas un libro que no existe?
- ¿Qué pasa cuando intentas eliminar un libro inexistente?

Vamos a verificar que todas esas reglas se cumplen, **sin importar qué haga la base de datos**.

---

## 🔧 Código Base: Tu `LibroService` actual

Para que todos partamos del mismo punto, este es el `LibroService` que deberías tener tras la Semana 2 (Labs 2 y 3):

```java
@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }

    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
    }

    public Optional<Libro> actualizar(Long id, Libro libroActualizado) {
        return libroRepository.findById(id).map(libroExistente -> {
            libroExistente.setTitulo(libroActualizado.getTitulo());
            libroExistente.setAutor(libroActualizado.getAutor());
            libroExistente.setIsbn(libroActualizado.getIsbn());
            libroExistente.setAnioPublicacion(libroActualizado.getAnioPublicacion());
            return libroRepository.save(libroExistente);
        });
    }

    public boolean eliminar(Long id) {
        if (libroRepository.existsById(id)) {
            libroRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

---

## 📝 Actividades

### Actividad 1 — Configurar el Test con Mocks

Crea el archivo: `src/test/java/com/librotech/service/LibroServiceTest.java`

```java
package com.librotech.service;

import com.librotech.model.Libro;
import com.librotech.repository.LibroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ← Activa Mockito para esta clase
class LibroServiceTest {

    @Mock // ← Crea un objeto FALSO de LibroRepository
    private LibroRepository libroRepository;

    @InjectMocks // ← Crea un LibroService REAL e inyecta el mock anterior
    private LibroService libroService;
}
```

#### 🔍 Explicación de las anotaciones:

| Anotación | ¿Qué hace? | Analogía |
|---|---|---|
| `@ExtendWith(MockitoExtension.class)` | Le dice a JUnit "voy a usar Mockito en esta clase" | Es como enchufar la máquina antes de usarla |
| `@Mock` | Crea una versión "de mentiras" del repositorio | Es el actor que interpreta el papel |
| `@InjectMocks` | Crea un servicio real y le inyecta el mock | Es poner al actor en el escenario |

> 🔑 **Concepto clave**: El `libroService` es REAL — su código se ejecuta de verdad. El `libroRepository` es FALSO — no toca ninguna base de datos. Lo que hacemos es decirle al mock "cuando alguien te llame con `findAll()`, devuelve ESTA lista".

---

### Actividad 2 — Probar `obtenerTodos()`: El Camino Feliz

```java
@Test
@DisplayName("obtenerTodos() debería retornar la lista de libros del repositorio")
void obtenerTodosDeberiaRetornarListaDeLibros() {
    // ========== GIVEN ==========
    // Creamos los datos que el mock va a devolver
    Libro libro1 = new Libro(1L, "Cien años de soledad", "García Márquez", "ISBN-001", 1967);
    Libro libro2 = new Libro(2L, "El Principito", "Saint-Exupéry", "ISBN-002", 1943);
    List<Libro> librosSimulados = Arrays.asList(libro1, libro2);

    // Le decimos al mock: "Cuando alguien llame a findAll(), devuelve esta lista"
    when(libroRepository.findAll()).thenReturn(librosSimulados);

    // ========== WHEN ==========
    // Ejecutamos el método REAL del servicio
    List<Libro> resultado = libroService.obtenerTodos();

    // ========== THEN ==========
    // Verificamos que el resultado es el esperado
    assertEquals(2, resultado.size());
    assertEquals("Cien años de soledad", resultado.get(0).getTitulo());
    assertEquals("El Principito", resultado.get(1).getTitulo());

    // Verificamos que el servicio SÍ llamó al repositorio exactamente 1 vez
    verify(libroRepository, times(1)).findAll();
}
```

#### 🔍 Desglose de los nuevos métodos:

**`when(...).thenReturn(...)`** — "Programar" el mock
```java
when(libroRepository.findAll()).thenReturn(librosSimulados);
// Traducción: "Cuando alguien llame a findAll() en el repositorio falso,
//              devuelve la lista librosSimulados"
```

**`verify(...)`** — Verificar que algo se llamó
```java
verify(libroRepository, times(1)).findAll();
// Traducción: "Confirma que findAll() se llamó exactamente 1 vez"
// Si el servicio nunca llamó al repositorio, el test FALLA
```

---

### Actividad 3 — Probar `obtenerTodos()`: Caso de lista vacía

```java
@Test
@DisplayName("obtenerTodos() debería retornar lista vacía cuando no hay libros")
void obtenerTodosDeberiaRetornarListaVacia() {
    // GIVEN - El repositorio devuelve una lista vacía
    when(libroRepository.findAll()).thenReturn(List.of());

    // WHEN
    List<Libro> resultado = libroService.obtenerTodos();

    // THEN
    assertNotNull(resultado, "El resultado no debería ser null, sino una lista vacía");
    assertTrue(resultado.isEmpty(), "La lista debería estar vacía");
    verify(libroRepository).findAll();
}
```

> 💡 **¿Por qué probamos la lista vacía?** Porque es un caso real: una biblioteca nueva no tiene libros aún. Nuestro servicio debe manejar esa situación sin errores.

---

### Actividad 4 — Probar `guardar()`: Guardado exitoso

```java
@Test
@DisplayName("guardar() debería llamar al repositorio y retornar el libro con ID asignado")
void guardarDeberiaRetornarLibroConId() {
    // GIVEN
    Libro libroSinId = new Libro(null, "Nuevo Libro", "Autor Nuevo", "ISBN-NEW", 2024);
    Libro libroConId = new Libro(1L, "Nuevo Libro", "Autor Nuevo", "ISBN-NEW", 2024);

    // El mock simula que el repositorio asigna un ID al guardar
    when(libroRepository.save(libroSinId)).thenReturn(libroConId);

    // WHEN
    Libro resultado = libroService.guardar(libroSinId);

    // THEN
    assertNotNull(resultado.getId(), "El libro guardado debería tener un ID asignado");
    assertEquals(1L, resultado.getId());
    assertEquals("Nuevo Libro", resultado.getTitulo());
    verify(libroRepository).save(libroSinId);
}
```

---

### Actividad 5 — Probar `obtenerPorId()`: Libro encontrado y no encontrado

```java
@Test
@DisplayName("obtenerPorId() debería retornar el libro cuando existe")
void obtenerPorIdCuandoExiste() {
    // GIVEN
    Libro libro = new Libro(1L, "1984", "George Orwell", "ISBN-1984", 1949);
    when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));

    // WHEN
    Optional<Libro> resultado = libroService.obtenerPorId(1L);

    // THEN
    assertTrue(resultado.isPresent(), "Debería encontrar el libro");
    assertEquals("1984", resultado.get().getTitulo());
}

@Test
@DisplayName("obtenerPorId() debería retornar vacío cuando el libro NO existe")
void obtenerPorIdCuandoNoExiste() {
    // GIVEN - El repositorio devuelve vacío (libro no encontrado)
    when(libroRepository.findById(999L)).thenReturn(Optional.empty());

    // WHEN
    Optional<Libro> resultado = libroService.obtenerPorId(999L);

    // THEN
    assertFalse(resultado.isPresent(), 
        "No debería encontrar un libro con ID inexistente");
    // También se puede escribir así:
    assertTrue(resultado.isEmpty());
}
```

> 🔑 **¿Por qué probamos ambos casos?** Porque en el mundo real, un bibliotecario puede buscar un libro que no existe. Nuestro servicio debe manejar ambas situaciones correctamente.

---

### Actividad 6 — Probar `eliminar()`: Éxito y fracaso

```java
@Test
@DisplayName("eliminar() debería retornar true cuando el libro existe")
void eliminarCuandoElLibroExiste() {
    // GIVEN
    when(libroRepository.existsById(1L)).thenReturn(true);

    // WHEN
    boolean resultado = libroService.eliminar(1L);

    // THEN
    assertTrue(resultado, "Debería retornar true al eliminar un libro existente");
    verify(libroRepository).deleteById(1L); // Verificar que SÍ se llamó al delete
}

@Test
@DisplayName("eliminar() debería retornar false cuando el libro NO existe")
void eliminarCuandoElLibroNoExiste() {
    // GIVEN
    when(libroRepository.existsById(999L)).thenReturn(false);

    // WHEN
    boolean resultado = libroService.eliminar(999L);

    // THEN
    assertFalse(resultado, "Debería retornar false cuando el libro no existe");
    verify(libroRepository, never()).deleteById(999L); // Verificar que NUNCA se llamó al delete
}
```

#### 🔍 Nuevo concepto: `verify(..., never())`

```java
verify(libroRepository, never()).deleteById(999L);
// Traducción: "Confirma que deleteById() NUNCA se llamó"
// Esto es importante porque si el libro no existe, 
// NO debemos intentar borrarlo
```

Tabla de verificadores de Mockito:

| Verificador | Significado |
|---|---|
| `times(1)` | Se llamó exactamente 1 vez |
| `times(3)` | Se llamó exactamente 3 veces |
| `never()` | Nunca se llamó |
| `atLeastOnce()` | Se llamó al menos 1 vez |
| `atMost(5)` | Se llamó como máximo 5 veces |

---

### Actividad 7 — Probar `actualizar()`: Camino feliz

```java
@Test
@DisplayName("actualizar() debería modificar los campos del libro existente")
void actualizarLibroExistente() {
    // GIVEN
    Libro libroExistente = new Libro(1L, "Título Viejo", "Autor Viejo", "ISBN-OLD", 2000);
    Libro datosNuevos = new Libro(null, "Título Nuevo", "Autor Nuevo", "ISBN-NEW", 2024);
    Libro libroActualizado = new Libro(1L, "Título Nuevo", "Autor Nuevo", "ISBN-NEW", 2024);

    when(libroRepository.findById(1L)).thenReturn(Optional.of(libroExistente));
    when(libroRepository.save(any(Libro.class))).thenReturn(libroActualizado);

    // WHEN
    Optional<Libro> resultado = libroService.actualizar(1L, datosNuevos);

    // THEN
    assertTrue(resultado.isPresent());
    assertEquals("Título Nuevo", resultado.get().getTitulo());
    assertEquals("Autor Nuevo", resultado.get().getAutor());
    verify(libroRepository).findById(1L);
    verify(libroRepository).save(any(Libro.class));
}
```

#### 🔍 Nuevo concepto: `any(Libro.class)`

```java
when(libroRepository.save(any(Libro.class))).thenReturn(libroActualizado);
// Traducción: "Cuando save() reciba CUALQUIER objeto de tipo Libro, 
//              devuelve libroActualizado"
// Usamos any() porque el objeto exacto que llega a save() 
// ha sido modificado dentro del servicio
```

---

### Actividad 8 — Práctica Autónoma

Escribe las siguientes pruebas por tu cuenta:

1. **`actualizar()` cuando el libro NO existe**: Debería retornar `Optional.empty()`. Verifica que `save()` NUNCA se llama.

2. **`guardar()` con libro null** (si tu servicio no lo maneja): ¿Qué pasa? ¿Debería lanzar una excepción? Este ejercicio te hará pensar en validaciones faltantes.

3. **Crea `EditorialServiceTest`**: Aplica el mismo patrón para probar los métodos del servicio de editoriales (si lo tienes implementado).

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se usa `@ExtendWith(MockitoExtension.class)` correctamente | ☐ |
| El repositorio está anotado con `@Mock` | ☐ |
| El servicio está anotado con `@InjectMocks` | ☐ |
| Se prueban al menos 2 métodos del servicio (happy path + error path) | ☐ |
| Se usa `when().thenReturn()` para programar los mocks | ☐ |
| Se usa `verify()` para confirmar las interacciones | ☐ |
| Todos los tests pasan exitosamente | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si no usas `@Mock` y creas un `LibroRepository` real? ¿El test funcionaría?
2. ¿Cuál es la diferencia entre `when().thenReturn()` y `when().thenThrow()`? ¿Cuándo usarías cada uno?
3. Si el `LibroService` tuviera una validación como `if (libro.getTitulo() == null) throw new RuntimeException("Título requerido")`, ¿cómo la probarías con Mockito?
4. ¿Por qué `verify()` es importante? ¿No basta con verificar el resultado?

---

## ➡️ Siguiente Fase

Ahora que sabes probar la lógica de negocio sin base de datos, avanza a **[Fase 3 — Testing del Repositorio con @DataJpaTest](./03-Fase3-Testing-Repositorio.md)**, donde probarás que tus consultas a la base de datos funcionan correctamente.
