# 🚀 FASE 3 — Ambientes, Secretos y Variables de Entorno

**Laboratorio Especial de CI/CD · Proyecto LibroTech 📚**  
**Duración estimada:** 1 hora  
**Nivel:** Intermedio  
**Aplica desde:** Semana 2 en adelante  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Crear **perfiles de Spring Boot** para diferentes ambientes (dev, test, prod).
2. Usar **variables de entorno** en GitHub Actions para configurar tu app.
3. Manejar **secretos** (contraseñas, API keys) de forma segura en el pipeline.
4. Entender por qué **nunca debes guardar contraseñas en el código fuente**.
5. Configurar tu pipeline para que use el perfil correcto automáticamente.

---

## 🧠 ¿Qué son los "Ambientes"?

En el mundo real, una aplicación no vive en un solo lugar. Tiene al menos 3 "ambientes" (entornos):

| Ambiente | ¿Para qué? | Base de datos | Ejemplo |
|---|---|---|---|
| **dev** (desarrollo) | Programar y probar localmente | H2 en memoria o local | Tu laptop |
| **test** (pruebas) | Ejecutar tests automatizados | H2 en memoria | El pipeline CI |
| **prod** (producción) | La app en vivo para usuarios reales | PostgreSQL / MySQL real | Servidor en la nube |

Cada ambiente necesita **configuración diferente**: diferente URL de base de datos, diferentes contraseñas, diferente nivel de logs.

### Analogía

Es como un restaurante con tres cocinas:
- **Cocina de prueba** (dev): Usas ingredientes baratos para experimentar recetas.
- **Cocina de ensayo** (test): Simulas el servicio completo con comensales de prueba.
- **Cocina de servicio** (prod): Comida real para clientes reales. Todo debe ser perfecto.

---

## 📖 Contexto de Negocio

**LibroTech** tiene un problema: el equipo de desarrollo usa una base de datos H2 local, pero producción usa PostgreSQL. Actualmente, alguien tiene que cambiar el `application.properties` a mano antes de cada despliegue. Esto ha causado incidentes donde se desplegó la configuración de desarrollo en producción. Necesitamos que la **configuración se seleccione automáticamente** según el ambiente.

---

## 📝 Actividades

### Actividad 1 — Crear Perfiles de Spring Boot

Spring Boot permite tener múltiples archivos de configuración. El archivo base es `application.properties`, y puedes crear variantes por ambiente:

**`src/main/resources/application.properties`** (configuración compartida):
```properties
# Configuración base — aplica a TODOS los ambientes
spring.application.name=LibroTech
server.port=8080

# El perfil activo se define por variable de entorno
# Si no se define, usa "dev" por defecto
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

**`src/main/resources/application-dev.properties`** (desarrollo):
```properties
# ===== PERFIL: DESARROLLO =====
# Base de datos H2 local — fácil y rápida para programar
spring.datasource.url=jdbc:h2:file:./data/librotech_dev
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true

# Logs detallados para debugging
logging.level.com.librotech=DEBUG
```

**`src/main/resources/application-test.properties`** (testing):
```properties
# ===== PERFIL: TESTING =====
# Base de datos H2 EN MEMORIA — se destruye al terminar
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.h2.console.enabled=false

# Desactivar Flyway en tests
spring.flyway.enabled=false

# Logs mínimos — solo errores
logging.level.com.librotech=WARN
```

**`src/main/resources/application-prod.properties`** (producción):
```properties
# ===== PERFIL: PRODUCCIÓN =====
# Base de datos PostgreSQL real
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.h2.console.enabled=false

# Flyway activo — migraciones profesionales
spring.flyway.enabled=true

# Logs solo de errores
logging.level.com.librotech=ERROR
```

#### 🔍 ¿Qué es `${DATABASE_URL}`?

```properties
spring.datasource.url=${DATABASE_URL}
```

Las llaves `${ }` indican que Spring debe leer el valor de una **variable de entorno del sistema operativo**. Esto significa que la contraseña de la base de datos de producción **NUNCA** se guarda en el código fuente — se inyecta desde fuera (desde el servidor o desde GitHub Secrets).

> ⚠️ **REGLA DE ORO**: Las contraseñas, API keys, tokens y URLs de producción **NUNCA** deben estar en tu código. Si alguien accede a tu repositorio (público o filtrado), tendría acceso a tu base de datos real.

---

### Actividad 2 — Activar el Perfil Correcto en el Pipeline

Actualiza tu `.github/workflows/ci.yml` para que el pipeline use el perfil `test`:

```yaml
      - name: 🧪 Ejecutar Tests
        run: mvn test -B
        env:
          SPRING_PROFILES_ACTIVE: test  # ← Usa el perfil de testing
```

Esto es equivalente a ejecutar:
```bash
SPRING_PROFILES_ACTIVE=test mvn test
```

Spring Boot leerá `application-test.properties` automáticamente.

---

### Actividad 3 — Configurar Secretos en GitHub

Los **Secrets** (secretos) de GitHub son variables cifradas que solo tu pipeline puede leer. Ni tú puedes ver su valor después de guardarlos.

**Paso 1: Crear un secreto en GitHub**
1. Ve a tu repositorio → **Settings** → **Secrets and variables** → **Actions**.
2. Haz clic en **"New repository secret"**.
3. Crea estos secretos (con valores de ejemplo):

| Nombre del secreto | Valor de ejemplo | ¿Para qué? |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://servidor:5432/librotech` | URL de la BD de producción |
| `DATABASE_USER` | `librotech_admin` | Usuario de la BD |
| `DATABASE_PASSWORD` | `SuperSecretPassword123!` | Contraseña de la BD |

**Paso 2: Usar los secretos en tu pipeline**

```yaml
  deploy:
    name: 🚀 Deploy a Producción
    runs-on: ubuntu-latest
    needs: test
    if: github.ref == 'refs/heads/main'  # Solo en la rama main

    steps:
      - uses: actions/checkout@v4

      - name: ☕ Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 📦 Empaquetar aplicación
        run: mvn package -DskipTests -B
        env:
          SPRING_PROFILES_ACTIVE: prod
          DATABASE_URL: ${{ secrets.DATABASE_URL }}
          DATABASE_USER: ${{ secrets.DATABASE_USER }}
          DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

#### 🔍 ¿Cómo funcionan los secretos?

```yaml
DATABASE_URL: ${{ secrets.DATABASE_URL }}
```

- `secrets.DATABASE_URL` lee el valor del secreto que configuraste en GitHub.
- El valor **nunca aparece en los logs**. Si intentas hacer `echo $DATABASE_URL` en un step, GitHub lo reemplaza con `***`.
- Los secretos solo están disponibles en el pipeline — nadie puede verlos en la interfaz de GitHub (ni siquiera tú, después de crearlos).

---

### Actividad 4 — Variables de Entorno (No Secretas)

Para configuraciones que NO son sensibles (no son contraseñas), puedes usar **Variables** en vez de Secrets:

1. Ve a **Settings** → **Secrets and variables** → **Actions** → pestaña **Variables**.
2. Crea variables como:

| Variable | Valor | ¿Para qué? |
|---|---|---|
| `JAVA_VERSION` | `17` | Para no hardcodear la versión de Java |
| `MAVEN_OPTS` | `-Xmx512m` | Para configurar la memoria de Maven |

Úsalas en el pipeline:

```yaml
      - name: ☕ Configurar JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ vars.JAVA_VERSION }}  # ← Lee la variable
          distribution: 'temurin'
```

### Diferencia entre Secrets y Variables:

| Característica | Secrets | Variables |
|---|---|---|
| ¿Se ve el valor después de guardarlo? | ❌ No (cifrado) | ✅ Sí |
| ¿Aparece en los logs? | ❌ No (oculto con `***`) | ✅ Sí |
| ¿Cuándo usar? | Contraseñas, tokens, API keys | Versiones, flags, configuraciones públicas |

---

### Actividad 5 — Verificar qué Perfil Está Activo

Agrega un paso de debug a tu pipeline para confirmar qué perfil se está usando:

```yaml
      - name: 🔍 Verificar perfil activo
        run: |
          echo "Perfil activo: $SPRING_PROFILES_ACTIVE"
          echo "Verificando archivos de configuración:"
          ls -la src/main/resources/application*.properties
        env:
          SPRING_PROFILES_ACTIVE: test
```

---

### Actividad 6 — Práctica Autónoma

1. **Crea el perfil `application-ci.properties`**: Un perfil específico para el pipeline CI, diferente al de test local. Podría tener logging más silencioso para no contaminar los logs del pipeline.

2. **Agrega un secreto ficticio**: Crea un secreto `APP_SECRET_KEY` en GitHub y úsalo en un step del pipeline. Verifica que no aparece en los logs.

3. **Prueba localmente con perfiles**: Ejecuta tu app con diferentes perfiles:
```bash
# Con perfil de desarrollo
mvn spring-boot:run

# Con perfil de testing
mvn spring-boot:run -Dspring.profiles.active=test
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Existen archivos `application-dev.properties`, `application-test.properties` y `application-prod.properties` | ☐ |
| El perfil de producción usa variables de entorno (`${DATABASE_URL}`) en vez de valores hardcodeados | ☐ |
| El pipeline usa `SPRING_PROFILES_ACTIVE: test` para los tests | ☐ |
| Se configuró al menos un secreto en GitHub | ☐ |
| No hay contraseñas ni datos sensibles en el código fuente | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si guardas la contraseña de la base de datos de producción directamente en `application-prod.properties` y subes ese archivo a GitHub?
2. ¿Cuál es la ventaja de usar `ddl-auto=create-drop` en testing vs `ddl-auto=validate` en producción?
3. Si un nuevo desarrollador se une al equipo y clona el repositorio, ¿podrá ver los secretos de producción? ¿Por qué es esto importante?
4. ¿Podrías tener un perfil diferente para cada desarrollador del equipo? ¿Tiene sentido hacerlo?

---

## ➡️ Siguiente Fase

Ahora que manejas ambientes y secretos, avanza a **[Fase 4 — Docker y Containerización](./04-Fase4-Docker-Containerizacion.md)**, donde empaquetarás tu aplicación Spring Boot en un contenedor Docker listo para desplegarse en cualquier servidor.
