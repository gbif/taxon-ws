# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GBIF Species API v2 — a Spring Boot 3.x REST API that wraps the ChecklistBank (CLB) PostgreSQL database with GBIF-compatible endpoints. It translates between GBIF UUID-based identifiers and ChecklistBank numeric IDs, exposing taxonomic data (names, synonyms, distributions, media, vernacular names) under `/species`.

## Build & Run Commands

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Build with tests (requires local PostgreSQL and Elasticsearch)
mvn clean install

# Run locally
mvn spring-boot:run

# Run with dev profile (connects to dev CLB database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate OpenAPI docs (runs during integration-test phase)
mvn verify
```

**Local prerequisites:** PostgreSQL on `localhost:5432/clb` (user: postgres/postgres), Elasticsearch on `localhost:9200` (index: clb).

## Architecture

### Request Flow

```
SpeciesResource (REST @RequestMapping("/species"))
  → SpeciesDao (business logic, model conversion)
    → DatasetKeyMap (Caffeine-cached UUID ↔ CLB numeric ID mapping)
    → SqlSessionFactory (MyBatis, using CLB's NameUsageMapper/DatasetMapper)
```

### Key Layers

- **`api/`** — DTOs returned by the API. `SimpleUsage` is the base; `NameUsage` extends it with full nomenclatural detail. `UsageInfo` is a composite DTO aggregating synonyms, vernacular names, distributions, media, and references.
- **`resource/`** — REST controllers. `SpeciesResource` handles `/species/{uuid}/{taxonKey}` endpoints.
- **`dao/`** — `SpeciesDao` queries CLB via MyBatis and converts CLB models (`NameUsageBase`) to API DTOs. `DatasetKeyMap` provides cached bidirectional UUID↔integer dataset ID translation.
- **`config/`** — `ClbConfig` sets up HikariCP datasource; `WebSecurityConfigurer` permits all requests with CSRF disabled; `WebMvcConfig` configures Jackson via GBIF's `JacksonJsonObjectMapperProvider` and allows semicolons in URLs.

### External Dependencies

- **ChecklistBank DAO** (`life.catalogue:dao`) — MyBatis mappers and CLB data models
- **GBIF API** (`org.gbif:gbif-api`) — Shared vocabularies and enums (Rank, TaxonomicStatus, etc.)
- **GBIF Common WS** (`org.gbif:gbif-common-ws`) — ObjectMapper config, exception handling

## Code Style

- Java 21, Lombok for DTOs (`@Data`)
- 2-space indentation (Java, SQL, XML, YAML) per `.editorconfig`
- Import order: `org.gbif` → `life.catalogue` → `java` → `jakarta` → `org` → `com` → others
- OpenAPI v3 annotations (`@Schema`) on all API model fields
- Spotless plugin enforces formatting

## CI/CD

Jenkins pipeline (`Jenkinsfile`): builds with Maven 3.9.9 + LibericaJDK21, auto-deploys dev branch to `species-ws-dev-deploy`. Releases run `mvn release:prepare release:perform` on master.
