# GBIF Species API v2 (`taxon-ws`)

A Spring Boot 3.5.x REST API that wraps the [ChecklistBank](https://checklistbank.org) (CLB) PostgreSQL database and Elasticsearch index with GBIF-compatible endpoints.
It translates between GBIF UUID-based dataset identifiers and ChecklistBank numeric IDs, exposing taxonomic data under `/taxon` and `/dataset/metrics`
for checklist datasets.

## Goals

**Goals:**
- Provide a GBIF v2 Species API backed by ChecklistBank as a simple read-only API.
- Expose name usages (scientific names, synonyms, vernacular names, distributions, media, references) for any checklist dataset registered in GBIF and indexed by ChecklistBank.
- Use Darwin Core terminology and vocabulary.
- Support full-text search and autocomplete over all indexed name usages via ChecklistBanks Elasticsearch index.
- Wrap the CLB API with GBIF dataset UUID keys, hiding the integer dataset keys.

**Non-goals:**
- Managing or editing taxonomic data — all data curation happens in ChecklistBank.
- Creating new search indices or any other artifacts to be managed.
- Replacing or duplicating ChecklistBank's own API — this service is a GBIF-specific adapter layer.


---

## How the Service Works

### High-level Architecture

```
Client
  │
  ▼
Spring Boot REST API (taxon-ws)
  │
  ├── TaxonResource    (/taxon/{datasetKey}/{taxonKey}/*)   — name usage detail & search
  ├── TreeResource     (/taxon/tree/{datasetKey}/*)         — taxonomic tree traversal
  └── DatasetResource  (/dataset/{datasetKey}/metrics)      — checklist-level statistics
  │
  ├── TaxonDao ──────────────────────────────────────────────────────────┐
  │     ├── MyBatis mappers → CLB PostgreSQL (name usages, synonyms,    │
  │     │                       distributions, media, vernacular names,  │
  │     │                       references, metrics)                     │
  │     └── CLB Elasticsearch client → full-text search & suggest        │
  │                                                                      │
  └── DatasetKeyMap ─── Caffeine in-memory cache ─── CLB PostgreSQL     │
        (translates GBIF UUID ↔ CLB integer dataset key)                │
                                                                         │
ChecklistBank PostgreSQL (clb)    ChecklistBank Elasticsearch (clb)  ◄──┘
```

MyBatis mappers and ES search services are reused from ChecklistBank.
The ApiConverter class maps CLB model objects to GBIF API DTOs and vice versa as needed.

### Request Flow

1. A client calls e.g. `GET /taxon/{datasetKey}/{taxonKey}` using a GBIF dataset UUID and a checklist-scoped taxon key.
2. `DatasetKeyMap` translates the GBIF UUID to the CLB integer dataset key (cached via Caffeine; loaded from the `dataset.gbif_key` column in CLB).
3. `TaxonDao` queries CLB via MyBatis mappers using the resolved integer dataset + taxon key.
4. `ApiConverter` maps CLB model objects (`NameUsageBase`, etc.) to GBIF API DTOs (`NameUsage`, `UsageInfo`, …).
5. The response is serialized to JSON using GBIF's shared Jackson configuration.

For search/suggest requests, the flow goes through the CLB Elasticsearch client instead of PostgreSQL.

### Key Components

| Layer | Package | Description |
|-------|---------|-------------|
| REST controllers | `resource/` | `TaxonResource` (name usage endpoints), `DatasetResource` (checklist metrics), `TreeResource` (tree traversal) |
| Data access | `dao/` | `TaxonDao` — orchestrates PostgreSQL (MyBatis) and Elasticsearch queries; `DatasetKeyMap` — UUID↔integer translation with Caffeine cache; `ApiConverter` — CLB model → GBIF DTO mapping |
| API DTOs | `api/` | `NameUsageSimple` (base), `NameUsage` (full detail), `UsageInfo` (composite), `Distribution`, `Media`, `VernacularName`, `Reference`, `ChecklistMetrics` |
| Configuration | `config/` | `ClbConfig` (HikariCP datasource), `WebMvcConfig` (Jackson, semicolons in URLs), `WebSecurityConfigurer` (permit-all, CSRF disabled) |

### External Dependencies

| Dependency | Coordinates | Purpose                                                             |
|------------|-------------|---------------------------------------------------------------------|
| ChecklistBank API | `life.catalogue:api` | CLB data models and enumerations                                    |
| ChecklistBank DAO | `life.catalogue:dao` | MyBatis mappers, search and DAO logic                               |
| GBIF API | `org.gbif:gbif-api` | Shared API classes (`PagingResponse`, `Pageable`)                   |
| GBIF NameParser API | `org.gbif:name-parser-api` | Shared vocabularies (`Rank`, `NameType`, `NomCode`)                 |
| GBIF Common WS | `org.gbif:gbif-common-ws` | `ObjectMapper` config, exception handling, Spring Boot 3.5.x integration |

---

## Checklist Sources

The service exposes data from **any checklist dataset registered in GBIF that is also indexed in ChecklistBank**.

### Dataset mapping (GBIF UUID → CLB integer key)

Every dataset in ChecklistBank can carry a `gbif_key` property containing the corresponding GBIF dataset UUID.
This is managed by the ChecklistBank registry sync and used to link CLB datasets with the corresponding GBIF registry entry.
`DatasetKeyMap` performs the translation by querying the CLB `dataset` table for a matching `gbif_key` and caches the result in a Caffeine in-memory cache.

#### Catalogue of Life (special case)

The [Catalogue of Life dataset in GBIF](https://www.gbif.org/dataset/7ddf754f-d193-4cc9-b351-99906754a03b) is mapped to the **latest published extended release** of COL in ChecklistBank (e.g. [`3LXR`](https://www.checklistbank.org/dataset/3LXR)).
CLB maintains a `LatestDatasetKeyCache` that always points to the current extended release; the mapping is resolved dynamically so no configuration change is needed when a new COL release is published.

A secondary GBIF UUID (`e007cc4a-8704-449d-8829-bb209d26d6c8`) maps to the **latest base release** of COL (the release without extended taxonomic coverage).

---

## Update Process

All data curation and publishing is managed by ChecklistBank — `taxon-ws` is read-only.

### PostgreSQL

ChecklistBank continuously ingests and processes checklist datasets.
Updates to the CLB PostgreSQL database are managed entirely by the CLB pipeline; `taxon-ws` connects as a read-only client.
No manual intervention is required in this service when data changes.

### Elasticsearch Index

The Elasticsearch index (`clb`) is populated and maintained by ChecklistBank's indexing pipeline.
When ChecklistBank re-indexes a dataset, the updated data becomes available to `taxon-ws` automatically — the service reads from the same shared index.
The index name is configured via `elasticsearch.index.name` in `application.yml`.

### Dataset key cache

`DatasetKeyMap` caches UUID↔integer mappings in memory (Caffeine).
If a dataset is newly registered or its GBIF key changes, a service restart will clear the cache and re-read from the database.

---

## OpenAPI Documentation

The API is documented using [springdoc-openapi](https://springdoc.org/) with OpenAPI v3 annotations (`@Operation`, `@Schema`, `@Parameter`) on all endpoints and DTO fields.

Generate the OpenAPI spec locally:

```bash
mvn verify -Popenapi
```

The generated spec is written to `target/openapi.yaml` and is published to [gbif.github.io/taxon-ws](https://gbif.github.io/taxon-ws/) via GitHub Pages as part of the release process.

Interactive Swagger UI is served at `/swagger-ui.html` when the application is running.

---

## Installation & Configuration

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | 21 | LibericaJDK or any JDK 21 distribution |
| Maven | 3.9.x | For build |
| PostgreSQL | 14+ | CLB database (`clb`) with read access |
| Elasticsearch | 9.x | CLB search index |

### Build

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Build including integration tests (requires local PostgreSQL and Elasticsearch)
mvn clean install
```

### Run

```bash
# Run with default profile (localhost PostgreSQL + Elasticsearch)
mvn spring-boot:run

# Run with dev profile (connects to GBIF dev CLB database and Elasticsearch)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Configuration

All settings live in `src/main/resources/application.yml`.
Override any property via environment variables or an external config file.

#### PostgreSQL

```yaml
datasource:
  url: jdbc:postgresql://localhost/clb
  username: postgres
  password: postgres
  hikari:
    maximumPoolSize: 4
```

#### Elasticsearch

```yaml
elasticsearch:
  hosts: localhost:9200          # comma-separated host:port list
  index:
    name: clb                    # index name
    numShards: 8
    numReplicas: 1
  connectTimeout: 8000
  socketTimeout: 10000
```

#### Zookeeper (optional, for service discovery)

```yaml
spring:
  cloud:
    zookeeper:
      enabled: true
      connect-string: localhost:2181
```


## CI/CD

Jenkins pipeline (`Jenkinsfile`) builds with Maven 3.9.9 + LibericaJDK 21.
The `dev` branch is auto-deployed to `taxon-ws-dev-deploy` on every successful build.
Releases are cut from `master` via `mvn release:prepare release:perform`.
