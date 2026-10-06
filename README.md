# Orazaka Users

> Reusable user management for any Krizaka application: registration, e-mail verification, login (password + Google/GitHub OAuth), forgot/reset password, profile & preferences, API keys, RBAC and JWT issuance.

**Layer:** Domain service — reusable by any Krizaka application · **Version:** `1.0.0-SNAPSHOT` · **License:** Apache-2.0 ·
part of the [Orazaka platform](https://github.com/krizaka/orazaka) by [Krizaka](https://krizaka.com)

## What it provides

A complete, reusable **user management** bounded context — the piece every application needs first.

| Capability | Endpoint |
|:---|:---|
| Register (e-mail verification token issued) | `POST /api/v1/auth/register` |
| Verify e-mail | `POST /api/v1/auth/verify` |
| Login (password → JWT) | `POST /api/v1/auth/login` |
| Login with Google / GitHub | `POST /api/v1/auth/oauth` |
| Forgot password (single-use, SHA-256 hashed, 15 min token) | `POST /api/v1/auth/forgot` |
| Reset password | `POST /api/v1/auth/reset` |
| Profile & preferences | `GET /api/v1/profile` · `PUT /api/v1/profile/preferences` |
| API keys | `GET/POST /api/v1/api-keys` · `DELETE /api/v1/api-keys/{id}` |
| Provider credentials (BYOK) | `GET/POST /api/v1/credentials` · `DELETE /api/v1/credentials/{provider}` |
| Service-to-service (SERVICE authority) | `/internal/v1/users/{id}` · `/internal/v1/tokens/exchange` · `/internal/v1/tiers/*` |

Domain events (`orazaka.events` exchange, via the transactional outbox): `evt.user.registered`,
`evt.password.reset` — consumed by [orazaka-notifications](https://github.com/krizaka/orazaka-notifications) to send the
verification and reset e-mails.

| Module | Role |
|:---|:---|
| `orazaka-identity-api` | Tier-1 contract: `User`, ports other services call. Depend on **this**, never on the implementation. |
| `orazaka-persistence-identity` | JPA entities & repositories of the identity database (`orazaka_identity_db`), identity outbox. |
| `orazaka-identity` | Domain + application services: registration, BCrypt, JWT, OAuth2 federation, RBAC, password recovery, profile. |
| `orazaka-identity-service` | Spring Boot host (port `8083`). |
| `infra/initdb/10-identity.sql` | Schema, role and dev seed of the identity database. |

## Use it

- **As a service** (recommended): run `orazaka-identity-service` and call the REST API above;
  other services depend only on `orazaka-identity-api`.
- **As a library**: embed `com.orazaka:orazaka-identity` in your own Spring Boot application.

```bash
./mvnw -pl orazaka-identity-service -am spring-boot:run
```

## Position in the platform

| | |
|:---|:---|
| Depends on | [`orazaka-build`](https://github.com/krizaka/orazaka-build) |
| Used by | [`orazaka-conversation-service`](https://github.com/krizaka/orazaka-conversation-service) · [`orazaka-job-service`](https://github.com/krizaka/orazaka-job-service) |
| Workspace path | `orazaka-apps/services/orazaka-users` |

## Build

**Inside the Orazaka workspace** (recommended — every dependency is built from source):

```bash
git clone https://github.com/krizaka/orazaka.git && cd orazaka
node scripts/workspace.mjs clone          # clones every repository at its workspace path
./mvnw -f orazaka-apps/services/orazaka-users/pom.xml verify
```

**Standalone** — upstream artifacts must be in `~/.m2` (built by the workspace) or resolvable from
GitHub Packages (`https://maven.pkg.github.com/krizaka/<repository>`, see the
[workspace README](https://github.com/krizaka/orazaka#consuming-packages)):

```bash
./mvnw verify
```

Requirements: JDK 21, Docker (Testcontainers integration tests).

## Governance

This repository follows the Orazaka governance contract — [AGENTS.md](https://github.com/krizaka/orazaka/blob/main/AGENTS.md)
in the workspace is normative; the local [AGENTS.md](AGENTS.md) only scopes it to this repository.

## License

Apache License 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE).
