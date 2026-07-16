# foundation-nats-starter

NATS messaging conventions for Spring Boot microservices.

Provides:
- A managed `Connection` bean (auto-closed on context shutdown)
- A `NatsMessagePublisher` bean for publishing messages wrapped in a standard envelope
- Envelope conventions: `id`, `correlationId`, `source`, `type`, `timestamp`, `payload`
- JSON serialization via Jackson
- Correlation ID propagation from request context into every outbound message

> **Scope**: this starter covers the **publisher** side only.
> The **subscriber** (consumer) side is intentionally the responsibility of the consuming service — see [Consumer conventions](#consumer-conventions).

---

## Usage

### 1. Declare the dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-nats-starter</artifactId>
</dependency>
```

The auto-configuration activates automatically when `io.nats.client.Connection` is on the classpath (i.e. when `jnats` is present via this starter).

---

### 2. Configure the server URL

```properties
foundation.nats.server-url=nats://nats-server:4222
```

```yaml
foundation:
  nats:
    server-url: nats://nats-server:4222
```

---

### 3. Publish a message

Inject `NatsMessagePublisher` and call `publish()`. The envelope is assembled automatically.

```java
@Service
public class QuoteService {

    private final NatsMessagePublisher publisher;

    public QuoteService(NatsMessagePublisher publisher) {
        this.publisher = publisher;
    }

    public void createQuote(QuoteDto quote, String correlationId) {
        // subject follows the convention: {domain}.{entity}.{action}
        publisher.publish("quote.created", "quote.created", correlationId, quote);
    }
}
```

If you do not have a correlation ID at the call site, use the overload without it — a UUID is generated automatically:

```java
publisher.publish("quote.created", "quote.created", quote);
```

---

## Message envelope

Every message is wrapped in a `MessageEnvelope` before being serialized to JSON:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "source": "quote-service",
  "type": "quote.created",
  "timestamp": "2026-07-14T10:00:00Z",
  "payload": { "quoteId": "Q-001", "amount": 1500.00 }
}
```

| Field | Description |
|---|---|
| `id` | Unique message identifier (UUID) — usable for idempotence checks |
| `correlationId` | Propagated from the incoming HTTP request (`X-Correlation-Id`) |
| `source` | Value of `spring.application.name` |
| `type` | Event or command type — use dot-separated lowercase names |
| `timestamp` | Publication time in UTC |
| `payload` | Business payload — any serializable object |

---

## Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `foundation.nats.server-url` | `String` | `nats://localhost:4222` | NATS server URL |

---

## Overriding beans

All beans are conditional. Declare your own bean to override:

```java
// custom Connection (e.g. with TLS options)
@Bean
public Connection natsConnection() throws Exception {
    Options options = new Options.Builder()
            .server("nats://nats-server:4222")
            .sslContext(mySslContext)
            .build();
    return Nats.connect(options);
}

// custom publisher (e.g. with additional headers or routing logic)
@Bean
public NatsMessagePublisher natsMessagePublisher(Connection connection, ObjectMapper objectMapper) {
    return new NatsMessagePublisher(connection, objectMapper, "my-service");
}
```

---

## Consumer conventions

> The subscriber implementation belongs to the **consuming service**, not to the foundation.

The foundation defines conventions for message consumers. Services that subscribe to NATS subjects must follow these rules:

### Deserializing the envelope

Use `ObjectMapper` to deserialize the received `byte[]` into `MessageEnvelope`:

```java
@Service
public class QuoteCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(QuoteCreatedConsumer.class);

    private final ObjectMapper objectMapper;

    public QuoteCreatedConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void onMessage(Message message) throws Exception {
        MessageEnvelope envelope = objectMapper.readValue(message.getData(), MessageEnvelope.class);

        // 1. Log the correlation ID at the start of processing
        MDC.put("correlationId", envelope.correlationId());
        log.info("Processing message id={} type={}", envelope.id(), envelope.type());

        try {
            // 2. Validate the envelope before processing
            if (envelope.payload() == null) {
                log.warn("Received message with null payload, skipping");
                return;
            }

            // 3. Process the payload (cast or re-deserialize to the expected type)
            QuoteDto quote = objectMapper.convertValue(envelope.payload(), QuoteDto.class);
            processQuote(quote);

        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### Consumer rules (from `nats-guidelines.md`)

| Rule | Reason |
|---|---|
| **Idempotent** | NATS may deliver the same message more than once. Use `envelope.id()` as an idempotence key (e.g. unique constraint in the database). |
| **Validate envelope** | Do not assume the envelope is well-formed. Check for null payload, unexpected type, etc. |
| **Log correlationId** | Set `MDC.put("correlationId", envelope.correlationId())` at the start so all logs for this message carry the correlation ID. |
| **Do not silently ignore errors** | Either handle the error, send to a DLQ subject, or let the message be redelivered (JetStream). |
| **Forward correlationId downstream** | Pass `envelope.correlationId()` to any HTTP or NATS calls made during processing. |

### JetStream (persistent messaging)

For durable, replayed, or DLQ-enabled messaging use JetStream via the `Connection` bean:

```java
JetStream js = connection.jetStream();
js.subscribe("quote.created", dispatcher, handler, false);
```

Configure max delivery attempts and DLQ subject in your JetStream consumer configuration.
The foundation does not impose a specific JetStream topology — that is a service-level concern.

---

## Subject naming

Follow the dot-separated, lowercase convention defined in `nats-guidelines.md`:

```
{domain}.{entity}.{action}

quote.created
policy.issued
payment.failed
```

Do not use generic subjects like `events` or `messages`.

---

## Error handling

Serialization failures in the publisher throw `FoundationTechnicalException`, which is mapped to HTTP 5xx by `foundation-api-starter`'s `GlobalExceptionHandler` when the call originates from an HTTP request handler.
