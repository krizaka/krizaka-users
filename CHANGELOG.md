# Changelog

All notable changes to this repository are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/).
Every Krizaka JVM artifact is released at the same version.

## [Unreleased]

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
