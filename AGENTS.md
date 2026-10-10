# krizaka-users — Scope (agent-neutral)

> A Krizaka building block: user management for any application, published on Maven Central as
> `com.krizaka:krizaka-users-*`. Orazaka is its first consumer, not its owner. When this repository is cloned inside
> the Orazaka workspace (`krizaka/krizaka-users`), the workspace contract
> ([`krizaka/orazaka/AGENTS.md`](https://github.com/krizaka/orazaka/blob/main/AGENTS.md)) applies as well, and its
> cross-repository rules scan this repository.

## Rules of this repository

- **Depends on Krizaka artifacts only** — `krizaka-build`, `krizaka-platform-kit`. Never on a product
  (`com.orazaka..`, `com.orochia..`): `UsersDependsOnNoProduct` fails the build.
- **Other services depend on the contract or the client** (`krizaka-users-api`, `krizaka-users-client`), never on
  `krizaka-users-core` or `-persistence`.
- **Hexagonal layout**: `domain` is framework-free; ports in `domain/ports`; adapters package-private in
  `infrastructure/adapter/<rest|amqp|persistence|federation|crypto>`; one top-level class per file
  (`krizaka-test-support` `CodeRules`, run by the `*GovernanceTest` suites).
- **Security** comes from `krizaka-security`: the filter chain starts with `SecurityBaseline.apply`; the internal surface
  requires `SERVICE`.
- **Configuration** lives under `krizaka.users.*` and `krizaka.users-service.*`, typed by `@ConfigurationProperties`
  records that validate themselves.
- **Owns its schema**: `infra/initdb/10-identity.sql`, read by the integration tests through `InitDb.locate`.
- **Services ship as Docker images, never on Maven Central.** Only the libraries (`-api`, `-client`, …) are published; a
  `*-service` host sets `maven.deploy.skip` and is listed in the root POM's `central-publishing-maven-plugin`
  `excludeArtifacts` (the plugin stages every module of the reactor otherwise). The `publishable-artifact-size` enforcer
  rule fails `verify` when a published jar exceeds 5 MB — a runnable (fat) jar never reaches Central.

## Definition of done

1. `./mvnw verify -Prelease -Dgpg.skip` is green (unit, Testcontainers integration tests, governance, javadoc).
2. Inside the Orazaka workspace, `./mvnw install` from the root is green — every consumer still builds.
3. [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md) describe the change.
