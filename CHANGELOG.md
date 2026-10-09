# Changelog

All notable changes to this repository are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/).
Every Krizaka JVM artifact is released at the same version.

## [Unreleased]

### Added

- The identity outbox implements `krizaka-messaging`'s `OutboxStore.append(NewOutboxMessage)`: a row written by an
  `EventPublisher` keeps its `messageId` and its envelope headers, and the relay publishes them as AMQP headers.
  `IdentityOutboxIT` proves it against the real `identity_outbox` table.

### Changed

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
