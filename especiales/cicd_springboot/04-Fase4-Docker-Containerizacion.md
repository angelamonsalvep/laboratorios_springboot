# 🚀 FASE 4 — Docker y Containerización de Spring Boot

**Laboratorio Especial de CI/CD · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio-Avanzado  
**Aplica desde:** Semana 3 en adelante  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender **qué es Docker** y por qué es esencial en CI/CD moderno.
2. Crear un **Dockerfile** optimizado para una aplicación Spring Boot.
3. Construir y ejecutar una **imagen Docker** de LibroTech.
4. Usar **multi-stage builds** para reducir el tamaño de la imagen.
5. Integrar la construcción de Docker en tu **pipeline de GitHub Actions**.

---

## 🧠 ¿Qué es Docker y por qué lo necesito?

### El Problema del "En mi máquina funciona" 🤷

```
Desarrollador: "La app funciona perfecto en mi laptop."
Servidor de producción: "Error: Java 17 no instalado. 
                         Error: dependencia X no encontrada."
```

Este problema ocurre porque tu laptop y el servidor tienen **configuraciones diferentes**: versión de Java, sistema operativo, variables de entorno, etc.

### La Solución: Contenedores

Un **contenedor Docker** es como una "caja" que incluye TODO lo que tu app necesita para funcionar:

```
Contenedor Docker de LibroTech:
┌─────────────────────────────────┐
│  🐧 Ubuntu (mini)              │
│  ☕ Java 17 (exacta)           │
│  📦 librotech.jar              │
│  ⚙️ Configuración              │
│  🔧 Dependencias               │
└─────────────────────────────────┘
```

Esta "caja" se ejecuta de **forma idéntica** en cualquier lugar: tu laptop, el servidor de un compañero, AWS, Google Cloud, etc.

### Analogía

- **Sin Docker**: Le das a alguien una receta de cocina y esperas que tenga los mismos ingredientes, el mismo horno y las mismas ollas que tú. Probablemente no los tenga.
- **Con Docker**: Le das un **plato ya cocinado en un tupper sellado**. Solo necesita abrirlo y calentarlo. Siempre sabe igual.

---

## 📖 Contexto de Negocio

**LibroTech** quiere desplegarse en la nube. El equipo de infraestructura dice: *"Necesitamos que la aplicación venga empaquetada en un contenedor Docker. No vamos a instalar Java manualmente en cada servidor."* Esta es la práctica estándar en la industria actual.

---

## 📝 Actividades

### Actividad 1 — Entender los Conceptos de Docker

Antes de escribir código, entiende estos 4 términos:

| Concepto | ¿Qué es? | Analogía |
|---|---|---|
| **Imagen** | Una "plantilla" inmutable con todo lo necesario para ejecutar tu app | La receta congelada lista para calentar |
| **Contenedor** | Una instancia en ejecución de una imagen | El plato caliente servido en la mesa |
| **Dockerfile** | El archivo de instrucciones para construir la imagen | La receta paso a paso |
| **Docker Hub** | Un registro público donde se almacenan imágenes | El supermercado de platos congelados |

El flujo es:
```
Dockerfile ──(build)──► Imagen ──(run)──► Contenedor
  📄                     📦                 🏃
```

---

### Actividad 2 — Crear el Dockerfile (Versión Simple)

Crea un archivo llamado `Dockerfile` en la **raíz** de tu proyecto (al mismo nivel que el `pom.xml`):

```dockerfile
# ============================================
# Dockerfile para LibroTech Spring Boot
# ============================================

# PASO 1: Usar una imagen base con Java 17
FROM eclipse-temurin:17-jre-alpine

# PASO 2: Crear un directorio de trabajo dentro del contenedor
WORKDIR /app

# PASO 3: Copiar el JAR de la aplicación al contenedor
# (Primero debes generar el JAR con: mvn package)
COPY target/*.jar app.jar

# PASO 4: Exponer el puerto 8080 (informativo)
EXPOSE 8080

# PASO 5: Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 🔍 Explicación de cada instrucción:

**`FROM eclipse-temurin:17-jre-alpine`**
```
¿Qué hace? Define la imagen base — un mini-Linux con Java 17 preinstalado
¿Por qué alpine? Es una versión ultraligera de Linux (~5 MB vs ~200 MB)
¿Por qué jre? Solo necesitamos el JRE (ejecutar), no el JDK (compilar)
```

**`WORKDIR /app`**
```
¿Qué hace? Crea y entra a la carpeta /app dentro del contenedor
Analogía: Es como hacer "mkdir /app && cd /app"
```

**`COPY target/*.jar app.jar`**
```
¿Qué hace? Copia el JAR generado por Maven al contenedor
Nota: Antes debes ejecutar "mvn package" para generar el JAR
```

**`EXPOSE 8080`**
```
¿Qué hace? Documenta que el contenedor escucha en el puerto 8080
Nota: NO abre el puerto — solo es informativo para quien lea el Dockerfile
```

**`ENTRYPOINT ["java", "-jar", "app.jar"]`**
```
¿Qué hace? Define el comando que se ejecuta cuando el contenedor arranca
Es como escribir "java -jar app.jar" en tu terminal
```

---

### Actividad 3 — Construir y Ejecutar la Imagen (Localmente)

**Paso 1: Genera el JAR de tu aplicación:**
```bash
mvn clean package -DskipTests
# -DskipTests = salta los tests para ir más rápido (ya los corrimos antes)
# El JAR se genera en target/librotech-0.0.1-SNAPSHOT.jar
```

**Paso 2: Construye la imagen Docker:**
```bash
docker build -t librotech:latest .
# -t librotech:latest = le da nombre "librotech" con etiqueta "latest"
# El punto final "." indica que el Dockerfile está en el directorio actual
```

**Paso 3: Ejecuta el contenedor:**
```bash
docker run -p 8080:8080 librotech:latest
# -p 8080:8080 = conecta el puerto 8080 de tu máquina con el del contenedor
```

**Paso 4: Prueba que funciona:**
Abre tu navegador en `http://localhost:8080/api/libros` o usa curl:
```bash
curl http://localhost:8080/api/libros
```

> 🎉 Tu aplicación LibroTech ahora corre dentro de un contenedor Docker.

---

### Actividad 4 — Multi-Stage Build (Dockerfile Profesional)

El Dockerfile anterior tiene un problema: necesitas compilar el JAR **antes** de construir la imagen. Con un **multi-stage build**, el propio Docker compila y empaqueta:

```dockerfile
# ============================================
# MULTI-STAGE BUILD — Profesional
# ============================================

# ── STAGE 1: COMPILAR (usando JDK completo) ──
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de configuración Maven primero (para aprovechar caché)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Descargar dependencias (se cachean si el pom.xml no cambia)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src/ src/

# Compilar y empaquetar (sin tests — ya se corrieron en el pipeline)
RUN ./mvnw package -DskipTests -B

# ── STAGE 2: EJECUTAR (usando JRE ligero) ──
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar SOLO el JAR del stage anterior (no el código fuente)
COPY --from=build /app/target/*.jar app.jar

# Configurar usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 🔍 ¿Qué es "multi-stage"?

```
Stage 1 (build):                    Stage 2 (runtime):
┌──────────────────────┐            ┌──────────────────────┐
│ JDK completo (400MB) │            │ JRE ligero (150MB)   │
│ Maven                │  ──JAR──►  │ Solo el .jar         │
│ Código fuente        │            │ Usuario no-root      │
│ Tests                │            │                      │
└──────────────────────┘            └──────────────────────┘
   Se descarta al final               Esta es la imagen final
```

**Ventajas:**
- La imagen final es **mucho más pequeña** (~150 MB vs ~500 MB).
- El código fuente **no está** en la imagen final (seguridad).
- No necesitas tener Maven instalado localmente.

---

### Actividad 5 — Agregar Docker al Pipeline de GitHub Actions

Agrega un nuevo job a tu `.github/workflows/ci.yml`:

```yaml
  # ────────────────────────────────────────
  # Job: Construir Imagen Docker
  # ────────────────────────────────────────
  docker:
    name: 🐳 Docker Build
    runs-on: ubuntu-latest
    needs: test  # Solo construye Docker si los tests pasaron

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: 🐳 Construir Imagen Docker
        run: docker build -t librotech:${{ github.sha }} .
        # github.sha = el hash del commit actual
        # Cada imagen tiene un tag único basado en el commit

      - name: 🔍 Verificar Imagen
        run: |
          docker images librotech
          echo "Imagen construida exitosamente ✅"
```

#### 🔍 ¿Qué es `github.sha`?

```yaml
docker build -t librotech:${{ github.sha }} .
# github.sha = "a1b2c3d4e5f6..." (el hash único del commit)
# Resultado: librotech:a1b2c3d4e5f6
```

Usar el hash del commit como tag de la imagen te permite:
- Saber exactamente qué versión del código tiene cada imagen.
- Hacer rollback a una versión anterior si algo falla en producción.

---

### Actividad 6 — El Archivo .dockerignore

Así como `.gitignore` le dice a Git qué archivos ignorar, `.dockerignore` le dice a Docker qué NO incluir al construir la imagen:

Crea el archivo `.dockerignore` en la raíz del proyecto:

```
# Archivos que Docker debe ignorar
.git
.github
.idea
.vscode
*.md
target/
data/
*.log
node_modules/
```

Esto hace que el build sea **más rápido** (no copia archivos innecesarios al contexto de Docker).

---

### Actividad 7 — Práctica Autónoma

1. **Ejecuta la imagen con variables de entorno:**
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:h2:mem:test \
  librotech:latest
```

2. **Verifica el tamaño de tu imagen:**
```bash
docker images librotech
# Compara el tamaño con multi-stage vs sin multi-stage
```

3. **Explora el contenedor por dentro:**
```bash
docker run -it librotech:latest /bin/sh
# Ahora estás DENTRO del contenedor — puedes ver los archivos
ls -la /app/
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Existe un `Dockerfile` en la raíz del proyecto | ☐ |
| La imagen se construye sin errores con `docker build` | ☐ |
| El contenedor arranca y responde en `localhost:8080` | ☐ |
| Se usa multi-stage build para optimizar el tamaño | ☐ |
| Existe `.dockerignore` con las exclusiones apropiadas | ☐ |
| El pipeline de GitHub Actions construye la imagen Docker | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Cuál es la diferencia entre una **imagen** y un **contenedor**? ¿Puedes tener múltiples contenedores de la misma imagen?
2. ¿Por qué usamos `eclipse-temurin:17-jre-alpine` en vez de `eclipse-temurin:17-jdk`? ¿Cuánto espacio ahorramos?
3. ¿Por qué el usuario del contenedor debería ser **no-root** (`USER spring:spring`)?
4. Si tu aplicación necesita conectarse a una base de datos PostgreSQL que corre en OTRO contenedor, ¿cómo lo harías? (Investiga `docker-compose`).

---

## ➡️ Siguiente Fase

Para la fase final, avanza a **[Fase 5 — Pipeline Profesional Completo](./05-Fase5-Pipeline-Completo-Profesional.md)**, donde integrarás todo lo aprendido en un pipeline de grado profesional con protección de ramas y deploy automatizado.
