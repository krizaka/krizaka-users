<!-- krizaka-header -->
<div align="center">

<img src="https://raw.githubusercontent.com/krizaka/.github/main/profile/assets/krizaka.svg" alt="Krizaka" width="72">

# Krizaka Users

**User management you do not have to write again.**

Registration with e-mail verification, password and Google/GitHub login, forgot/reset password, profile and
preferences, API keys, provider credentials, RBAC and JWT issuance — as a Spring Boot service, a library, a contract
and a typed client.

[![CI](https://github.com/krizaka/krizaka-users/actions/workflows/ci.yml/badge.svg)](https://github.com/krizaka/krizaka-users/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.krizaka/krizaka-users-api?color=3b82f6&label=maven%20central)](https://central.sonatype.com/namespace/com.krizaka)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

[Open source at Krizaka](https://www.krizaka.com/en/open-source) · [Website](https://www.krizaka.com) · [Krizaka on GitHub](https://github.com/krizaka)

</div>
<!-- /krizaka-header -->

Built for and used by [Orazaka](https://github.com/krizaka/orazaka), the sovereign AI engine; usable by any Spring Boot application.

## What it does

| Capability | Endpoint |
|:---|:---|
| Register (an e-mail verification token is issued) | `POST /api/v1/auth/register` |
| Verify the e-mail address | `POST /api/v1/auth/verify` |
| Log in with a password → session JWT | `POST /api/v1/auth/login` |
| Log in with Google or GitHub | `POST /api/v1/auth/oauth` |
| Forgot password (single-use token, SHA-256 hashed, 15 minutes) | `POST /api/v1/auth/forgot` |
| Reset password | `POST /api/v1/auth/reset` |
| Profile and preferences | `GET /api/v1/profile` · `PUT /api/v1/profile/preferences` |
| API keys | `GET/POST /api/v1/api-keys` · `DELETE /api/v1/api-keys/{id}` |
| Provider credentials (bring your own key, encrypted at rest) | `GET/POST /api/v1/credentials` · `DELETE /api/v1/credentials/{provider}` |
| Service-to-service — `SERVICE` authority only | `/internal/v1/users/{id}` · `/internal/v1/tokens/exchange` · `/internal/v1/tiers/*` |

Passwords are hashed with BCrypt, provider keys encrypted with AES-256, sessions are HS256 JWTs carrying a `roles`
claim that [`krizaka-security`](https://github.com/krizaka/krizaka-platform-kit) verifies locally in every other
service. Domain events leave through a transactional outbox (relayed by `krizaka-messaging`): `evt.user.registered`
and `evt.password.reset`, which [`krizaka-notifications`](https://github.com/krizaka/krizaka-notifications) turns into
the verification and reset e-mails.

## Modules

| Artifact | Published | Role |
|:---|:--:|:---|
| `com.krizaka:krizaka-users-api` | ✓ | The contract: `User`, `UserProfile`, `RateLimitInfo` and the `UserDirectoryClient` port. Other services depend on **this**. |
| `com.krizaka:krizaka-users-client` | ✓ | `UserDirectoryClient` over HTTP: `SERVICE` token on every call, per-entry cache. Spring Boot auto-configuration. |
| `com.krizaka:krizaka-users-core` | ✓ | Domain and application services: registration, BCrypt, JWT, OAuth federation, RBAC, password recovery, profile. Embed it to host users inside your own application. |
| `com.krizaka:krizaka-users-persistence` | ✓ | JPA entities and repositories of the users database, and its outbox. |
| `krizaka-users-service` | — | The Spring Boot host (port `8083`). Built from source. |
| `infra/initdb/10-identity.sql` | — | Schema, role and development seed of the users database. |

## Call it from another service

```xml
<dependency>
    <groupId>com.krizaka</groupId>
    <artifactId>krizaka-users-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

```yaml
krizaka:
  users:
    client:
      base-url: http://users:8083
      service-secret: ${IDENTITY_JWT_SECRET}   # the shared HS256 secret
      service-name: billing-service           # who is calling (the token's subject)
      cache-ttl: PT60S
```

```java
@Service
class InvoiceService {
  private final UserDirectoryClient users;
  InvoiceService(UserDirectoryClient users) { this.users = users; }

  String recipient(String userId) {
    return users.getUser(userId).email();
  }
}
```

## Run the service

```bash
./mvnw -pl krizaka-users-service -am spring-boot:run
```

| Property | Environment | Default |
|:---|:---|:---|
| `krizaka.users.jwt.secret` | `IDENTITY_JWT_SECRET` | — (≥ 32 characters, required) |
| `krizaka.users.jwt.issuer` | `IDENTITY_JWT_ISSUER` | `krizaka-users` |
| `krizaka.users.jwt.ttl` | `IDENTITY_JWT_TTL` | `PT12H` |
| `krizaka.users.crypto.key` / `.salt` | `CRYPTO_KEY` / `CRYPTO_SALT` | — (provider-key encryption) |
| `krizaka.users.email-verification.enabled` | `EMAIL_VERIFICATION_ENABLED` | `true` |
| `krizaka.users-service.datasource.url` | `IDENTITY_DB_URL` | `jdbc:postgresql://localhost:5432/orazaka_identity_db` |
| `krizaka.users-service.datasource.username` / `.password` | `IDENTITY_DB_USERNAME` / `IDENTITY_DB_PASSWORD` | `orazaka_identity` / — |
| `server.port` | `IDENTITY_PORT` | `8083` |

PostgreSQL and RabbitMQ are required. The database, its role (created **without** a password — set it at deployment)
and the schema come from [`infra/initdb/10-identity.sql`](infra/initdb/10-identity.sql).

> **Wire names.** The database (`orazaka_identity_db`), its tables and the events exchange (`orazaka.events`) keep the
> names of the platform this service was extracted from, so existing deployments keep their data and their consumers.
> They are deployment settings, not code: renaming them is a data migration, planned separately.

## Build

```bash
./mvnw verify                        # unit and Testcontainers integration tests (Docker required)
./mvnw verify -Prelease -Dgpg.skip   # + the sources and javadoc jars Maven Central requires
```

It inherits [`krizaka-parent`](https://github.com/krizaka/krizaka-build) and uses
[`krizaka-platform-kit`](https://github.com/krizaka/krizaka-platform-kit): build those first, or let CI do it. JDK 21.

## Contributing

Issues and pull requests are welcome — see the organisation's
[contributing guide](https://github.com/krizaka/.github/blob/main/CONTRIBUTING.md) and
[security policy](https://github.com/krizaka/.github/blob/main/SECURITY.md).

## License

[Apache License 2.0](LICENSE) © 2026 Krizaka
