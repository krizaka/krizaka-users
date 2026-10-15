# Changelog

All notable changes to this repository are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/).
Every Krizaka JVM artifact is released at the same version.

## [Unreleased]

### Added

- **Account administration** (`/api/v1/admin/users`, administrators only, every action audited): search and list,
  suspend and restore (the account can no longer sign in or use a key), grant or withdraw the administrator role (never
  your own, the last administrator keeps it), Law 25 export and erasure of the identity context's data (the typed
  e-mail confirms; the audit keeps the fact, not the person), and the audit trail (`GET /audit`). New table
  `admin_audit`: `infra/initdb/10-identity.sql`, and `infra/migrations/2026-10-15-admin-audit.sql` for an existing
  database — apply it before deploying. Sessions are stateless tokens: a token already issued is not revoked by a
  suspension in the services that only verify its signature. `IdentityServiceContextIT` starts the whole service as
  deployed and drives the administration through its HTTP chain.

- `krizaka.users.registration.enabled` (`AUTH_SIGNUP_ENABLED`, default `true`): `false` closes account creation —
  `POST /api/v1/auth/register` and the first federated sign-in of an unknown identity answer 403 `Registration is closed`,
  nothing is written; existing accounts keep signing in. Deployed environments run with it off.

- Event contracts: `krizaka-users-api` publishes the JSON Schema (draft 2020-12) of each event it emits —
  `events/evt.user.registered.v1.json`, `events/evt.password.reset.v1.json` — and `UserEventsContractTest`
  (`krizaka-test-support` `EventContractTest`) checks that the events serialised by the users service conform.
- The identity outbox implements `krizaka-messaging`'s `OutboxStore.append(NewOutboxMessage)`: a row written by an
  `EventPublisher` keeps its `messageId` and its envelope headers, and the relay publishes them as AMQP headers.
  `IdentityOutboxIT` proves it against the real `identity_outbox` table.

### Changed

- Built on `krizaka-parent` and `krizaka-platform-kit` **0.2.0** (released on Maven Central); this repository's
  version follows the parent (0.2.0, not yet released).
- The service host is never published to Maven Central: it is excluded from the Central bundle by name
  (`excludeArtifacts`) and ships as a Docker image; the `publishable-artifact-size` enforcer rule fails `verify` when a
  published jar exceeds 5 MB.
- `identity_outbox` gains `headers JSONB NOT NULL DEFAULT '{}'` (`infra/initdb/10-identity.sql`). An existing database
  needs `ALTER TABLE identity_outbox ADD COLUMN IF NOT EXISTS headers JSONB NOT NULL DEFAULT '{}'::jsonb;`.

## [0.1.0]

First release as a Krizaka building block (formerly `orazaka-users`, part of the Orazaka platform).

### Added

- `krizaka-users-client`: typed HTTP client of the internal API (`UserDirectoryClient`), authenticated with a
  `SERVICE` token, cached per entry — replaces the two copies Orazaka's services carried.
- `UserDirectoryClient` port in `krizaka-users-api`.

### Changed

- Coordinates `com.krizaka:krizaka-users-*` (was `com.orazaka:orazaka-identity*`), packages `com.krizaka.users.*`.
- Configuration under `krizaka.users.*` and `krizaka.users-service.datasource.*` (was `orazaka.identity.*`).
- Security chain built on `krizaka-security`'s `SecurityBaseline`: the CORS preflight and `/error` are now open, as
  in every other service.
- Outbox relayed and messages deduplicated by `krizaka-messaging`.
- Default JWT issuer `krizaka-users` (set `IDENTITY_JWT_ISSUER` to keep another).
- `UserProfile` is `(userId, theme, attributes)`: application-defined attributes replace the AI-assistant fields
  (`voiceModel`, `primaryIndustry`, `aiBehavior`), which the hosting application now stores as attributes.
- Onboarding and feedback forms are the application's (`USERS_ONBOARDING_SCHEMA`, `USERS_FEEDBACK_SCHEMA`); the
  built-in ones ask for a theme, a language and a display name.
- Reserved preference namespaces are declared by the application (`krizaka.users.preferences.reserved-prefixes`).
- Queues and exchanges: events go to `krizaka.messaging.exchanges.events`; database `krizaka_users_db`, role
  `krizaka_users`, tables without a product prefix.

### Removed

- `user_model_prefs` and its persistence code: never read by any service.
