# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GBIF Species API v2 — a Spring Boot 3.5.x REST API that wraps the ChecklistBank (CLB) PostgreSQL database with GBIF-compatible endpoints.
It translates between GBIF UUID-based identifiers and ChecklistBank numeric IDs, exposing taxonomic data (names, synonyms, distributions, media, vernacular names) under `/species`.

## Terminology & documentation
Terminology and semantics of fields are based on Darwin Core (DwC) if possible: https://dwc.tdwg.org/terms/
For some DTOs DwC/GBIF extension definitions have been used: https://rs.gbif.org/extensions.html
Otherwise the Catalogue of Life Data Package (ColDP) specification is used: https://github.com/CatalogueOfLife/coldp/blob/master/README.md

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
mvn verify -Popenapi
```

**Local prerequisites:** PostgreSQL on `localhost:5432/clb` (user: postgres/postgres), Elasticsearch on `localhost:9200` (index: clb).

## Architecture

### Key Layers

- **`api/`** — DTOs returned by the API. `SimpleUsage` is the base; `NameUsage` extends it with full nomenclatural detail. `UsageInfo` is a composite DTO aggregating synonyms, vernacular names, distributions, media, and references.
- **`resource/`** — REST controllers. `SpeciesResource` handles `/species/{uuid}/{taxonKey}` endpoints.
- **`dao/`** — `SpeciesDao` queries CLB via MyBatis and converts CLB models (`NameUsageBase`) to API DTOs. `DatasetKeyMap` provides cached bidirectional UUID↔integer dataset ID translation.
- **`config/`** — `ClbConfig` sets up HikariCP datasource; `WebSecurityConfigurer` permits all requests with CSRF disabled; `WebMvcConfig` configures Jackson via GBIF's `JacksonJsonObjectMapperProvider` and allows semicolons in URLs.

### External Dependencies

- **ChecklistBank API** (`life.catalogue:api`) — CLB data models and enumerations.
- **ChecklistBank DAO** (`life.catalogue:dao`) — MyBatis mappers and DAO logic.
- **GBIF API** (`org.gbif:gbif-api`) — Shared GBIF API classes (PagingResponse, Pageable): https://github.com/gbif/gbif-api
- **GBIF NameParser API** (`org.gbif:name-parser-api`) — Shared vocabularies and enums (Rank, NameType, NomCode): https://github.com/gbif/name-parser
- **GBIF Common WS** (`org.gbif:gbif-common-ws`) — ObjectMapper config, exception handling, HandlerMethodArgumentResolver, Spring Boot 3.5.x: https://github.com/gbif/gbif-common-ws

## Code Style

- Java 21, Lombok for DTOs (`@Data`)
- 2-space indentation (Java, SQL, XML, YAML) per `.editorconfig`
- Import order: `org.gbif` → `life.catalogue` → `java` → `jakarta` → `org` → `com` → others
- OpenAPI v3 annotations (`@Schema`) on all API model fields
- Spotless plugin enforces formatting

## CI/CD

Jenkins pipeline (`Jenkinsfile`): builds with Maven 3.9.9 + LibericaJDK21, auto-deploys dev branch to `species-ws-dev-deploy`. Releases run `mvn release:prepare release:perform` on master.
