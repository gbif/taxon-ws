# GBIF Species API v2

The new GBIF Species API v2 connecting to the ChecklistBank Postgres database.
It is a Spring Boot 3.x REST API that wraps the ChecklistBank (CLB) PostgreSQL database with GBIF-compatible endpoints.
It translates between GBIF UUID-based dataset identifiers and ChecklistBank numeric IDs, exposing taxonomic data (names, synonyms, distributions, media, vernacular names) under `/species`.
