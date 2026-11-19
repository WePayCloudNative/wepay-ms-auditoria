# WePay — Microservicio de Auditoría

![Java](https://img.shields.io/badge/Java-25-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-green) ![Kafka](https://img.shields.io/badge/Apache%20Kafka-Consumer-black) ![Database](https://img.shields.io/badge/BD-H2%20%7C%20Oracle-blue)

Microservicio de **auditoría y trazabilidad de eventos** del proyecto **WePay**, desarrollado para el caso académico de arquitectura cloud native. Recibe eventos de negocio publicados en Apache Kafka, guarda un registro histórico del evento original y ofrece consultas REST de solo lectura para revisar la actividad general, la historia de un pedido y las acciones asociadas a un usuario.

**Repositorio:** [WePayCloudNative/wepay-ms-auditoria](https://github.com/WePayCloudNative/wepay-ms-auditoria)  
**Estado documentado:** 19 de noviembre de 2026. Funcionalidad principal probada en entorno local; integración y persistencia en Oracle/AWS aún no verificadas.

## 1. Funcionalidades

- Consumo de eventos desde el tópico Kafka **`orders.events`** mediante el grupo **`wepay-auditoria-group`**.
- Lectura del contenido JSON y extracción de identificador del evento, tipo, usuario, pedido y fecha del evento.
- Persistencia de cada evento con su **payload JSON original**, usuario, entidad, origen y marca de tiempo.
- **Idempotencia básica por `eventId`**: consulta previa de existencia y restricción única en base de datos.
- Consultas HTTP de solo lectura: todos los eventos, historial por pedido/entidad e historial por usuario.
- Soporte para **H2 en memoria** durante el desarrollo y configuración base de **Oracle** en `application.yml`.

> Este servicio consume `orders.events`. El tópico `audit.timeline` forma parte de la arquitectura general planteada para WePay, pero **este repositorio no contiene actualmente un productor que publique en ese tópico**. Tampoco se ha configurado un Dead Letter Topic (DLT) propio de auditoría.

## 2. Tecnologías

| Componente | Versión o implementación |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4.1.1 |
| API HTTP | Spring Web MVC |
| Mensajería | Spring Kafka / Apache Kafka |
| Persistencia | Spring Data JPA / Hibernate |
| Desarrollo local | Base de datos H2 en memoria |
| Base de datos prevista para integración | Oracle Database; driver JDBC `ojdbc17` |
| Compilación | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| JSON | `tools.jackson.databind.ObjectMapper` y `JsonNode` (Jackson 3) |

**Puerto HTTP predeterminado:** `8086`.  
**Group ID Kafka:** `wepay-auditoria-group`.  
**Tópico Kafka consumido:** `orders.events`.

## 3. Arquitectura y flujo de datos

```text
wepay-ms-pedidos
       │
       │ publica eventos de pedido
       ▼
 Apache Kafka
 topic: orders.events
       │
       │ @KafkaListener
       ▼
 AuditoriaConsumer
       │  interpreta JSON y conserva el mensaje original
       ▼
 AuditoriaService
       │  valida eventId y evita duplicados
       ▼
 EventoAuditoriaRepository (JPA)
       │
       ▼
 Base de datos
  ├── H2 (perfil local)
  └── Oracle (configuración general; requiere validar despliegue)
       │
       ▼
 AuditoriaController
 GET /api/audit
 GET /api/audit/entidad/{entidadId}
 GET /api/audit/usuario/{oid}
```

La auditoría utiliza su propio grupo de consumidores Kafka y puede procesar los eventos independientemente del microservicio de reportería.

## 4. Estructura del proyecto

```text
wepay-ms-auditoria/
├── src/
│   ├── main/
│   │   ├── java/cl/duoc/wepay_ms_auditoria/
│   │   │   ├── consumer/
│   │   │   │   └── AuditoriaConsumer.java
│   │   │   ├── controller/
│   │   │   │   └── AuditoriaController.java
│   │   │   ├── dto/
│   │   │   │   └── EventoAuditoriaResponse.java
│   │   │   ├── entity/
│   │   │   │   └── EventoAuditoria.java
│   │   │   ├── repository/
│   │   │   │   └── EventoAuditoriaRepository.java
│   │   │   └── service/
│   │   │       └── AuditoriaService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml
│   └── test/                         # pruebas según contenido del proyecto
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md                         # este documento al incorporarlo
```

### Responsabilidades de las clases

| Clase | Responsabilidad |
|---|---|
| `AuditoriaConsumer` | Escucha `orders.events`, interpreta el JSON y delega el registro. |
| `AuditoriaService` | Comprueba duplicados por `eventId`, persiste los datos y genera las respuestas de consulta. |
| `EventoAuditoria` | Entidad JPA que representa el registro histórico almacenado. |
| `EventoAuditoriaRepository` | Repositorio JPA con búsquedas por usuario y entidad, ordenadas por fecha descendente. |
| `EventoAuditoriaResponse` | DTO de respuesta de las consultas REST. |
| `AuditoriaController` | Expone tres endpoints HTTP GET bajo `/api/audit`. |

## 5. Requisitos para ejecución local

- JDK **25** instalado (`java -version`).
- Docker con un broker Kafka operativo (por ejemplo, desde el repositorio de orquestación de WePay).
- Acceso desde Windows a Kafka en `localhost:9092`, salvo que se configure otro host.
- Maven Wrapper incluido en el proyecto; no es imprescindible instalar Maven globalmente.
- Puerto `8086` disponible.

### Clonar el proyecto

```powershell
 git clone https://github.com/WePayCloudNative/wepay-ms-auditoria.git
 cd wepay-ms-auditoria
```

### Comprobar el broker Kafka

Si el contenedor del entorno local se llama `kafka`:

```powershell
 docker ps
 docker exec kafka kafka-topics --bootstrap-server kafka:29092 --list
```

El tópico `orders.events` debe existir. La dirección `kafka:29092` es la conexión **dentro de Docker** usada en las pruebas; el microservicio ejecutado desde Windows utiliza `localhost:9092` cuando no se define otro valor.

### Compilar y ejecutar con H2

**Este paso es esencial:** el archivo `application.yml` contiene Oracle como configuración base. Para utilizar H2 debe activarse expresamente el perfil `local`.

```powershell
 .\mvnw.cmd clean compile
 .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Alternativa en PowerShell:

```powershell
 $env:SPRING_PROFILES_ACTIVE = "local"
 .\mvnw.cmd spring-boot:run
```

La aplicación se inicia en **http://localhost:8086**. Los datos de H2 se mantienen solo mientras la instancia está funcionando.

Para construir el JAR:

```powershell
 .\mvnw.cmd clean package
```

## 6. Configuración de la aplicación

### `application.yml` — Configuración general (Oracle)

En la versión revisada, este archivo contiene:

- `server.port=8086`.
- URL Oracle construida con `DB_HOST`, `DB_PORT` y `DB_NAME`.
- Usuario y contraseña basados en `DB_USER` y `DB_PASSWORD`.
- `spring.jpa.hibernate.ddl-auto=update`.
- Servidores Kafka definidos por `KAFKA_SERVERS` (predeterminado: `localhost:9092`).
- `app.kafka.topic.orders=orders.events`.

Ejemplo de variables utilizadas en la configuración existente:

```dotenv
DB_HOST=servidor-oracle
DB_PORT=1521
DB_NAME=XE
DB_USER=usuario_auditoria
DB_PASSWORD=valor_seguro
KAFKA_SERVERS=broker1:9092,broker2:9092,broker3:9092
```

> **Seguridad:** `application.yml` tiene valores de respaldo para credenciales de Oracle. Para un despliegue real, deben retirarse los valores inseguros, inyectar secretos mediante variables o gestor de secretos y evitar publicar claves. Para producción se recomienda validar/migrar un esquema controlado, en lugar de depender de `ddl-auto=update`.

### `application-local.yml` — H2

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:auditoriadb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

El perfil `local` sobrescribe la conexión Oracle con la base H2 en memoria.

> No se identificó un archivo `application-prod.yml` en el repositorio al momento de redactar esta documentación. La conexión real a Oracle y la persistencia tras reinicios siguen pendientes de verificación.

## 7. Contrato de eventos Kafka

El consumidor espera mensajes JSON como el siguiente:

```json
{
  "eventId": "audit-demo-001",
  "tipoEvento": "OrderCreated",
  "pedidoId": 300,
  "usuarioOid": "usuario-test",
  "total": 79990.0,
  "estado": "CREADO",
  "fechaCreacion": "2026-11-19T12:00:00",
  "fechaEvento": "2026-11-19T12:00:00",
  "detalles": []
}
```

**Campos empleados directamente por auditoría:**

| Campo | Uso |
|---|---|
| `eventId` | Identificador único para evitar registros repetidos. |
| `tipoEvento` | Nombre de la acción (`OrderCreated`, `OrderDelivered`, etc.). |
| `pedidoId` | Identificador de la entidad auditada, almacenado como texto. |
| `usuarioOid` | Identificador del usuario asociado al evento. |
| `fechaEvento` | Fecha y hora original del evento; se interpreta como `LocalDateTime`. |
| JSON completo | Se conserva en `payload` para trazabilidad. |

Los campos restantes no se descartan del historial: permanecen en el `payload` original. El campo `origen` se guarda con el valor `wepay-ms-pedidos` en el consumidor actual.

### Envío manual de un evento de prueba

```powershell
 docker exec -it kafka kafka-console-producer --bootstrap-server kafka:29092 --topic orders.events
```

Cuando aparezca `>`, pega el JSON anterior en una sola línea, sin saltos internos. Para pruebas repetidas usa un `eventId` nuevo. Si vuelves a enviar exactamente el mismo `eventId`, el servicio debe ignorar el duplicado.

**Atención:** el consumidor exige un JSON bien formado y una `fechaEvento` compatible con `LocalDateTime.parse`, como `2026-11-19T12:00:00`. Un mensaje vacío se ignora; no hay en este repositorio una configuración DLT equivalente a la implementada en reportería.

## 8. API REST

**URL base:** `http://localhost:8086/api/audit`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/audit` | Lista todos los eventos registrados. |
| GET | `/api/audit/entidad/{entidadId}` | Historial de un pedido o entidad, del más reciente al más antiguo. |
| GET | `/api/audit/usuario/{oid}` | Historial asociado a un usuario, del más reciente al más antiguo. |

No se han definido métodos POST, PUT o DELETE en el controlador de auditoría. La entrada habitual de nuevos registros se realiza a través de Kafka.

### Consultar todos los eventos

```powershell
 curl.exe "http://localhost:8086/api/audit"
```

### Consultar la trazabilidad de un pedido

```powershell
 curl.exe "http://localhost:8086/api/audit/entidad/300"
```

### Consultar las acciones de un usuario

```powershell
 curl.exe "http://localhost:8086/api/audit/usuario/usuario-test"
```

### Ejemplo de respuesta

La siguiente respuesta es **ilustrativa**; los identificadores, fechas y contenido dependen de los eventos almacenados:

```json
[
  {
    "id": 1,
    "usuarioOid": "usuario-test",
    "tipoEvento": "OrderCreated",
    "entidadId": "300",
    "fechaEvento": "2026-11-19T12:00:00",
    "origen": "wepay-ms-pedidos",
    "payload": "{\"eventId\":\"audit-demo-001\",\"tipoEvento\":\"OrderCreated\",\"pedidoId\":300,\"usuarioOid\":\"usuario-test\",\"fechaEvento\":\"2026-11-19T12:00:00\"}"
  }
]
```

**Nota:** el DTO público `EventoAuditoriaResponse` no expone `eventId` como propiedad independiente; este identificador sí se conserva en la entidad y dentro del `payload` original.

Cuando no existen coincidencias, los endpoints devuelven una lista vacía (`[]`).

## 9. Persistencia y modelo de datos

Entidad **`EventoAuditoria`**, tabla **`auditoria_eventos`**:

| Atributo JPA | Propósito |
|---|---|
| `id` | Clave primaria `Long`, autogenerada con `GenerationType.IDENTITY`. |
| `eventId` | Identificador de Kafka, con restricción única. |
| `usuarioOid` | Usuario que originó la acción. |
| `tipoEvento` | Acción o tipo de evento. |
| `entidadId` | ID del pedido o entidad, almacenado como `String`. |
| `fechaEvento` | Fecha de ocurrencia del evento (`LocalDateTime`). |
| `origen` | Sistema que produjo la acción. |
| `payload` | Mensaje JSON original (`@Lob`). |

El repositorio `EventoAuditoriaRepository` implementa consultas mediante Spring Data JPA y la comprobación `existsByEventId`.

### Idempotencia

`AuditoriaService.registrarEvento()` utiliza una transacción, verifica si el `eventId` ya existe y persiste mediante `saveAndFlush`. Se probaron mensajes duplicados localmente y el servicio registró el aviso `Evento duplicado ignorado`.

La comprobación previa y la restricción única protegen contra duplicación básica; ante escrituras concurrentes, una inserción puede fallar por clave única y requerir tratamiento adicional.

### Limitación de H2

H2 usa `jdbc:h2:mem:auditoriadb`. Sus registros desaparecen al terminar el proceso. Los offsets del grupo consumidor Kafka pueden sobrevivir al reinicio, por lo que el historial **no se reconstruye automáticamente** en un H2 recién creado. Oracle o una base persistente es necesaria para conservar la auditoría real.

## 10. Pruebas funcionales realizadas

Durante las pruebas locales del proyecto se verificó:

| Prueba | Resultado observado |
|---|---|
| Inicio de aplicación con H2 y Kafka | Funcionamiento local comprobado. |
| Consumo de un `OrderCreated` desde `orders.events` | Evento recibido y persistido. |
| Duplicación de un `eventId` (`audit-test-002`) | Evento duplicado ignorado según logs. |
| Consulta `GET /api/audit/entidad/300` | Devolvió el registro de auditoría con su `fechaEvento` original. |
| Consulta de historial mediante controlador REST | Endpoint de lectura funcional. |

**No se afirma** que el despliegue Oracle, AWS, seguridad de endpoints o recuperación ante fallos Kafka hayan sido validados en producción.

## 11. Consideraciones de seguridad y operación

- El controlador REST no implementa autenticación/autorización directamente en las clases revisadas. La protección del API deberá integrarse con el mecanismo de seguridad o gateway del sistema antes de exponerlo públicamente.
- El campo `payload` conserva el mensaje completo; evitar incluir secretos o datos sensibles innecesarios en los eventos y restringir quién puede consultar estos registros.
- Las consultas `GET /api/audit` devuelven todos los registros sin paginación; para alto volumen conviene incorporar paginación y filtros.
- Los errores de JSON o fechas inválidas deben manejarse con una política explícita de reintentos y DLT para no bloquear el consumo.
- Para ambientes compartidos, definir retención, respaldos, acceso de solo lectura y trazabilidad operacional.
- El consumidor actual no publica mensajes en `audit.timeline`; este aspecto debe acordarse si se requiere para la rúbrica.

## 12. Trabajo pendiente antes de producción

- [ ] Probar conexión real a Oracle y persistencia de datos tras reiniciar el microservicio.
- [ ] Separar o endurecer la configuración de producción y retirar credenciales de respaldo inseguras.
- [ ] Preparar y validar migraciones o esquema Oracle compatibles con las entidades JPA.
- [ ] Definir reintentos y DLT para mensajes inválidos en auditoría.
- [ ] Verificar el requisito arquitectónico del tópico `audit.timeline`, si aplica a la integración completa.
- [ ] Validar autenticación y autorización de los endpoints a través del mecanismo de seguridad de WePay.
- [ ] Integrar el microservicio con BFF/API Gateway y probar despliegue AWS.
- [ ] Agregar pruebas automatizadas y verificaciones de integración Kafka/base de datos.
- [ ] Revisar observabilidad, política de retención y paginación para historiales grandes.

## 13. Referencias del proyecto

- [Organización WePayCloudNative en GitHub](https://github.com/WePayCloudNative)
- [Repositorio de auditoría](https://github.com/WePayCloudNative/wepay-ms-auditoria)
- [Repositorio de reportería](https://github.com/WePayCloudNative/wepay-ms-reportes)

---

**Alcance:** este documento describe el código publicado en el repositorio de auditoría y las pruebas locales realizadas hasta el 19 de noviembre de 2026. La configuración AWS/Oracle se documenta como prevista y pendiente de comprobación, no como desplegada.
