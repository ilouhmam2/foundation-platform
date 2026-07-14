# NATS Guidelines - foundation-platform

## 1. Purpose

This document defines NATS messaging standards for services using `foundation-platform`.

The goal is to standardize:

- subject naming
- message envelope
- publishing
- consuming
- correlation ID propagation
- retries
- DLQ
- idempotence

## 2. Messaging principles

Use messaging for:

- asynchronous processing
- event notifications
- decoupling services
- long-running processing
- fan-out use cases

Do not use messaging just because it is available.

If the frontend needs an immediate response, a synchronous API may be more appropriate.

## 3. Commands vs events

A command represents an intention.

Example:

```text
CreateQuoteCommand
```

An event represents something that has already happened.

Example:

```text
QuoteCreatedEvent
```

Use events to notify other services. Use commands to trigger processing within a single service context.

---

## 4. Subject naming

Use dot-separated, lowercase subject names:

```text
{domain}.{entity}.{action}

quote.created
quote.updated
policy.issued
payment.failed
```

Do not use generic subjects like `events` or `messages`.

---

## 5. Message envelope

Wrap all NATS messages in a standard envelope:

```json
{
  "id": "uuid",
  "correlationId": "request-correlation-id",
  "source": "quote-service",
  "type": "quote.created",
  "timestamp": "2026-07-14T10:00:00Z",
  "payload": { ... }
}
```

The foundation provides the envelope model and serialization conventions.

---

## 6. Correlation ID propagation

Always propagate `X-Correlation-Id` from the incoming request into the NATS message envelope.

Consumers must forward the correlation ID to any downstream calls.

---

## 7. Publisher conventions

- Serialize payload to JSON
- Populate the envelope before publishing
- Handle publish errors explicitly
- Do not publish from inside a database transaction without an outbox pattern

---

## 8. Consumer conventions

- Consumers must be idempotent
- Validate the envelope before processing
- Log the correlation ID at the start of processing
- Do not silently ignore errors

---

## 9. Retry and DLQ

NATS JetStream provides persistent messaging with replay.

Recommendations:
- Configure max delivery attempts
- Route failed messages to a DLQ subject
- Monitor DLQ for unprocessed messages

---

## 10. Idempotence

Every consumer must handle duplicate message delivery.

Strategies:
- Idempotence key in the database (unique constraint)
- Check-and-skip based on message ID
- Outbox table for DB + event atomicity