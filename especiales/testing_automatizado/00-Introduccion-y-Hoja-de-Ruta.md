# 🧪 LABORATORIO ESPECIAL — Testing Automatizado en Spring Boot

**Módulo 6.1 · Laboratorio Especial · Proyecto LibroTech 📚**  
**Duración estimada:** 8–10 horas (dividido en 7 fases progresivas)  
**Nivel:** Desde principiante hasta avanzado  

---

## 📋 ¿De qué trata este laboratorio?

Este laboratorio te guiará paso a paso para agregar **pruebas automatizadas** a tu proyecto **LibroTech** — el mismo que construiste en las semanas anteriores. La idea es simple: **no vas a crear funcionalidades nuevas**, sino asegurarte de que todo lo que ya hiciste funciona correctamente, y que si algo se rompe en el futuro, te enterarás inmediatamente.

Piensa en las pruebas automatizadas como un **guardia de seguridad** para tu código. Cada vez que hagas un cambio, ese guardia revisa todo y te avisa si algo dejó de funcionar.

---

## 🎯 ¿Por qué es tan importante?

Imagina esta situación real en LibroTech:

> Un desarrollador modifica el método `actualizar()` del `LibroService` para agregar una validación nueva. Sin querer, introduce un bug que hace que el precio del libro siempre quede en `0.0`. Sin pruebas, este error llega a producción y los clientes ven todos los libros gratis. Con pruebas automatizadas, el error se detecta **antes** de hacer deploy.

---

## 🏗️ La Pirámide de Testing

Las pruebas se organizan como una pirámide. Las de abajo son las más rápidas y baratas; las de arriba son más lentas pero prueban el sistema completo:

```
        ┌─────────────────────┐
        │   🖥️ E2E / API     │  ← Pocas, lentas, prueban TODO el flujo
        │    (MockMvc)        │
        ├─────────────────────┤
        │  🔗 Integración     │  ← Medias, prueban capas conectadas
        │  (@DataJpaTest)     │
        ├─────────────────────┤
        │  ⚙️ Unitarias       │  ← Muchas, rápidas, prueban UNA cosa
        │ (Mockito + JUnit)   │
        └─────────────────────┘
          Mayor cantidad abajo
        = menor costo y mayor velocidad
```

En este laboratorio seguiremos ese orden: **empezamos desde la base** (pruebas unitarias simples) y subimos hasta **pruebas de API completas**.

---

## 📚 ¿Qué necesitas tener ANTES de empezar?

Este laboratorio asume que ya completaste los laboratorios anteriores y que tu proyecto LibroTech tiene:

| Componente | Semana donde se creó |
|---|---|
| Entidad `Libro` (con `@Entity`, campos: titulo, autor, isbn, etc.) | Semana 2, Lab 2 |
| Entidad `Editorial` y `Genero` (relaciones) | Semana 4, Lab 1 |
| `LibroRepository` (extiende `JpaRepository`, con `findByAutor`) | Semana 2, Lab 2 |
| `LibroService` (con `guardar`, `obtenerTodos`, `obtenerPorId`, `actualizar`, `eliminar`) | Semana 2, Labs 2-3 |
| `LibroController` (CRUD completo con `ResponseEntity`) | Semana 2, Lab 3 |
| Paginación (`Page<Libro>`, `Pageable`) | Semana 2, Lab 4 |
| Soft Delete (`@SQLRestriction`, `descatalogarLibro`) | Semana 4, Lab 1 |

> 💡 **No te preocupes si no tienes todo exactamente igual.** Cada fase te dará el código base necesario para que puedas seguir el laboratorio sin problemas.

---

## 🗺️ Hoja de Ruta — Las 7 Fases

Cada fase es un archivo independiente que puedes completar en sesiones separadas. Están ordenadas de menor a mayor complejidad:

| Fase | Archivo | ¿Qué aprenderás? | Duración |
|---|---|---|---|
| **Fase 1** | `01-Fase1-Fundamentos-JUnit.md` | Escribir tu primera prueba con JUnit 5. Entender `@Test`, asserts y el patrón Given-When-Then | ~45 min |
| **Fase 2** | `02-Fase2-Testing-Unitario-Servicio.md` | Usar **Mockito** para probar el `LibroService` SIN base de datos. Entender Mocks y la anotación `@Mock` | ~1.5 h |
| **Fase 3** | `03-Fase3-Testing-Repositorio.md` | Probar el `LibroRepository` contra una base de datos real en memoria con `@DataJpaTest` | ~1 h |
| **Fase 4** | `04-Fase4-Testing-Controlador.md` | Probar los endpoints REST con `MockMvc` sin levantar un servidor real | ~1.5 h |
| **Fase 5** | `05-Fase5-Testing-Integracion-Completo.md` | Prueba de integración completa con `@SpringBootTest` — todo el sistema conectado | ~1.5 h |
| **Fase 6** | `06-Fase6-Cobertura-Buenas-Practicas.md` | Medir la cobertura de código con JaCoCo, organizar la suite y buenas prácticas | ~1 h |
| **Fase 7** | `07-Fase7-Automatizacion-CI.md` | Automatizar la ejecución de tests con Maven profiles y conceptos de CI/CD | ~45 min |

---

## 🔧 Dependencias necesarias en tu `pom.xml`

Antes de empezar cualquier fase, asegúrate de tener estas dependencias. La mayoría ya vienen incluidas con el **Spring Boot Starter Test**:

```xml
<!-- Esta dependencia trae JUnit 5, Mockito, AssertJ, MockMvc y más -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Base de datos en memoria para tests (si no la tienes ya) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

> 📌 **Nota sobre `<scope>test</scope>`**: Esto significa que estas dependencias SOLO se incluyen cuando ejecutas pruebas. No se empaquetan en tu aplicación final, así que no hacen tu `.jar` más pesado.

---

## 📂 Estructura de carpetas para los tests

Tu proyecto debería tener esta estructura. Los tests van en `src/test/java`, replicando la misma estructura de paquetes que tienes en `src/main/java`:

```
src/
├── main/java/com/librotech/
│   ├── model/
│   │   ├── Libro.java
│   │   ├── Editorial.java
│   │   └── Genero.java
│   ├── repository/
│   │   └── LibroRepository.java
│   ├── service/
│   │   └── LibroService.java
│   └── controller/
│       └── LibroController.java
│
└── test/java/com/librotech/        ← 🧪 AQUÍ VAN TUS TESTS
    ├── model/
    │   └── LibroTest.java                  ← Fase 1
    ├── service/
    │   └── LibroServiceTest.java           ← Fase 2
    ├── repository/
    │   └── LibroRepositoryTest.java        ← Fase 3
    ├── controller/
    │   └── LibroControllerTest.java        ← Fase 4
    └── integration/
        └── LibroIntegrationTest.java       ← Fase 5
```

---

## 🚀 ¿Listo? Comienza con la Fase 1

Abre el archivo **`01-Fase1-Fundamentos-JUnit.md`** y da tu primer paso en el mundo del testing automatizado.

> 🧠 **Consejo**: No intentes hacer todas las fases de una vez. Cada fase construye sobre la anterior. Tómate tu tiempo para entender cada concepto antes de avanzar.
