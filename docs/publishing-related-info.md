# Publishing data for the GBIF Taxon API `RelatedInfo`

This guide is for **content publishers** who want their data to surface in the GBIF
Taxon API v2 under the *related information* of a taxon.

The `/species/{datasetKey}/{taxonID}/related` endpoint returns a
[`RelatedInfo`](../src/main/java/org/gbif/taxon/api/RelatedInfo.java) object with three
sections, each aggregated from a different external source:

| Field | Source | What it carries |
|-------|--------|-----------------|
| `redlist` | The **IUCN Red List** dataset | the global threat status (LC, NT, VU, EN, CR, …) |
| `cites`   | The three **CITES appendix** datasets | the appendix (I, II, III) a taxon is listed under |
| `griis`   | All datasets of the **GRIIS** publisher | per-country introduced / invasive distribution records |

This document explains exactly how the service finds and reads that data, and how to
structure a **ColDP** package or a **Darwin Core Archive (DwC-A)** so it is picked up.

> The mapping described here was traced through the ChecklistBank importer
> (`life.catalogue.importer.coldp.ColdpInterpreter`,
> `life.catalogue.importer.dwca.DwcInterpreter`, `InterpreterBase`) and the
> `taxon-ws` aggregation code in
> [`TaxonDao.listRelatedInfo`](../src/main/java/org/gbif/taxon/dao/TaxonDao.java).

---

## 1. How a taxon is matched to your data — by name, not by ID

You do **not** reference any GBIF backbone key, taxonKey, or external identifier in your
data. ChecklistBank links a taxon a user is viewing to records in *your* dataset purely
through the **ChecklistBank names index** (`nidx`).

When the API builds `RelatedInfo` for a taxon it asks ChecklistBank for *related* usages —
name usages in other datasets whose scientific name maps to the **same names‑index entry**.
The matching:

- normalises the scientific name and compares it together with **authorship** and **rank**;
- also matches through the **canonical** name, so authorship variants of the same canonical
  name still link;
- ignores private datasets.

**What this means for you as a publisher:**

1. Provide a clean, parseable `scientificName` (or atomised name parts) for every record.
2. Provide the `authorship` — it improves match precision and disambiguates homonyms.
3. Provide the correct `rank`.
4. Use the accepted name. Synonyms are matched too, but the related-info lookups are run for
   accepted taxa, so list the taxon under the name the target taxonomy is most likely to use.

If the names don't match in the index, none of the sections below will populate, no matter
how correct the rest of the record is.

---

## 2. `redlist` — IUCN Red List threat status

### What the service does

```
related.iucn = 19491596-35ae-4a91-9a98-85cf505f1bd3   (configurable)
```

1. Find the usage in the IUCN dataset whose name matches the taxon.
2. Read that usage's **distribution** records.
3. Pick the record whose **area name equals `Global`** (case-insensitive) **and** that has a
   `threatStatus`.
4. Expose that usage as `redlist`, with `threatStatus` copied from the global distribution.

So two things are required per taxon: a name that matches, and **one distribution record for
the area `Global` carrying the threat status**.

> Only the dataset registered as `related.iucn` is consulted for this field. In practice this
> is the official IUCN Red List checklist; the structure below is what that dataset must
> contain.

### Accepted `threatStatus` values

`EX`, `EW`, `CR`, `EN`, `VU`, `NT`, `LC`, `DD`, `NE`
(IUCN category codes; the full names such as `LEAST_CONCERN` are also accepted.)

### ColDP example

`NameUsage.tsv`

```tsv
ID	scientificName	authorship	rank	status	parentID
iucn-22823	Panthera leo	(Linnaeus, 1758)	species	accepted	iucn-felidae
```

`Distribution.tsv` — note `gazetteer` is `text` and `area` is the literal `Global`:

```tsv
taxonID	gazetteer	area	threatStatus	referenceID
iucn-22823	text	Global	VU	iucn-assessment-2023
```

That single global row is what produces `redlist.threatStatus = "VU"`. You may add further
distribution rows for individual countries/regions — they are ignored for the global threat
status but are kept as ordinary distributions.

### DwC-A example

Use the **Distribution extension**
(`http://rs.gbif.org/extension/gbif/1.0/distribution.xml`). The threat status uses the IUCN
term `http://iucn.org/terms/threatStatus`:

`distribution.txt`

```tsv
taxonID	locality	threatStatus
22823	Global	VU
```

In `meta.xml` map the columns to `dwc:locality` and `iucn:threatStatus`. A row with no
`locationID`/`countryCode` and `locality = Global` is interpreted as a free‑text (`TEXT`)
area named `Global`, which is exactly what the service looks for.

---

## 3. `cites` — CITES appendix listings

### What the service does

```
related.citesI   = 314512    (Appendix I)
related.citesII  = 314531    (Appendix II)
related.citesIII = 314533    (Appendix III)
```

For each of the three appendix datasets, the service looks for a usage matching the taxon.
**The appendix (I / II / III) is decided by *which dataset* the match comes from — not by any
field in your data.** If a match is found in the Appendix II dataset, the taxon gets a `cites`
entry with `citesAppendix = "II"`.

Therefore CITES data is modelled as **three separate ChecklistBank datasets**, one per
appendix. To list a species in an appendix, simply include it (name‑matchable) in the
corresponding dataset.

### What is exposed

Only the name usage is returned (scientific name, authorship, rank, status) plus its
**`link`** as `references`. Add a `link` per taxon pointing at the authoritative page (e.g.
the Species+ taxon concept) so the API can expose it.

### ColDP example (the **Appendix II** dataset)

`NameUsage.tsv`

```tsv
ID	scientificName	authorship	rank	status	link
sp-10836	Panthera leo	(Linnaeus, 1758)	species	accepted	https://www.speciesplus.net/#/taxon_concepts/10836
```

Being present in this dataset is sufficient; nothing in the record names the appendix.
A taxon listed in more than one appendix will appear once per dataset it is in (each becomes
a separate entry in the `cites` array with its own `citesAppendix`).

### DwC-A example

A minimal taxon core is enough. Map `dwc:scientificName`, `dwc:scientificNameAuthorship`,
`dwc:taxonRank`, `dwc:taxonomicStatus`, and `dc:references` (→ `link`):

`taxa.txt`

```tsv
taxonID	scientificName	scientificNameAuthorship	taxonRank	taxonomicStatus	references
10836	Panthera leo	(Linnaeus, 1758)	species	accepted	https://www.speciesplus.net/#/taxon_concepts/10836
```

---

## 4. `griis` — Global Register of Introduced and Invasive Species

### What the service does

```
related.griisPublisherKey = cdef28b1-db4e-4c58-aa71-3c5238c2d0b5   (configurable)
```

1. Find **all** related usages across **every dataset published by the GRIIS publisher**
   (GRIIS is published as one checklist **per country**).
2. For each matched usage, read its distribution records and keep those where
   **`establishmentMeans` resolves to `INTRODUCED` (or one of its subtypes)** and an area is
   present.
3. Determine the **country** from the **dataset's `country_XX` metadata keyword** — *not* from
   the distribution record (see below).
4. Read the **`isInvasive`** flag from a taxon property (see below).

Each kept distribution becomes one `griis` entry.

### Requirement A — one dataset per country, tagged with `country_XX`

The country reported for a GRIIS record is taken from a dataset **keyword** of the form
`country_XX`, where `XX` is the ISO‑3166 alpha‑2 code. Every GRIIS country checklist must
carry this keyword in its metadata.

ColDP `metadata.yaml`:

```yaml
title: GRIIS — Country Checklist of United States
keyword:
  - GRIIS
  - invasives
  - country_US
```

(For a DwC-A, add the same keyword to the EML `<keywordSet>`.)

### Requirement B — `establishmentMeans` must resolve to `INTRODUCED` (or a subtype)

Only distributions whose `establishmentMeans` resolves to `INTRODUCED` **or one of its
subtypes** are included; native, vagrant and uncertain records are ignored.

Values recognised as `INTRODUCED`:

`introduced`, `alien`, `exotic`, `invasive`, `non-native`, `nonindigenous`

Values recognised as the subtype `INTRODUCED_ASSISTED_COLONISATION` (also included):

`assisted`, `iac`, `introduced (assisted colonisation)`

`establishmentMeans` is a hierarchical vocabulary — any present or future child of
`INTRODUCED` counts. When in doubt, use plain `introduced`.

### Requirement C — the `isInvasive` flag

`isInvasive` is read from an `isInvasive` taxon property whose value is one of `yes`, `true`,
`1`, `invasive`, `isinvasive` (case-insensitive). If absent, the record is still returned,
just without `isInvasive = true`. Both formats can supply it.

**ColDP** — add a `TaxonProperty` record named `isInvasive`:

`TaxonProperty.tsv`

```tsv
taxonID	property	value
griis-us-553	isInvasive	true
```

**DwC-A** — use the **Species Profile extension**
(`http://rs.gbif.org/extension/species_profile.xml`) with the term
`http://rs.gbif.org/terms/1.0/isInvasive`:

`speciesprofile.txt`

```tsv
taxonID	isInvasive
griis-us-553	true
```

Map the column to `gbif:isInvasive` in `meta.xml`. Both encodings resolve to the same
`isInvasive` taxon property that the API reads.

### Full ColDP example for one GRIIS country dataset

`metadata.yaml`

```yaml
title: GRIIS — Country Checklist of United States
keyword:
  - GRIIS
  - country_US
```

`NameUsage.tsv`

```tsv
ID	scientificName	authorship	rank	status
griis-us-553	Sus scrofa	Linnaeus, 1758	species	accepted
```

`Distribution.tsv` — the area code itself need not be the country (the country comes from the
keyword), but `establishmentMeans` must be `introduced`:

```tsv
taxonID	gazetteer	areaID	area	establishmentMeans	degreeOfEstablishment	pathway
griis-us-553	iso	US	United States	introduced	established	escape_from_cultivation
```

`TaxonProperty.tsv`

```tsv
taxonID	property	value
griis-us-553	isInvasive	true
```

This yields a `griis` distribution with `countryCode = US`, `establishmentMeans = introduced`,
and `isInvasive = true`.

### DwC-A equivalent

- **Core**: the taxon with `dwc:scientificName`, `dwc:scientificNameAuthorship`,
  `dwc:taxonRank`.
- **Distribution extension**: `dwc:establishmentMeans = introduced`
  (`dwc:countryCode`/`dwc:locationID` optional — country is taken from the dataset keyword).
- **Species Profile extension**: `gbif:isInvasive = true`.
- **EML**: add the `country_XX` keyword.

---

## 5. Quick reference

### Distribution columns

| ColDP (`Distribution`) | DwC term | Used for |
|------------------------|----------|----------|
| `areaID` + `gazetteer` | `dwc:locationID` / `dwc:countryCode` | coded area |
| `area`                 | `dwc:locality` / `dwc:country` | free‑text area (e.g. `Global`) |
| `establishmentMeans`   | `dwc:establishmentMeans` | GRIIS introduced filter |
| `degreeOfEstablishment`| `dwc:degreeOfEstablishment` | passthrough |
| `pathway`              | `dwc:pathway` | passthrough |
| `threatStatus`         | `iucn:threatStatus` | IUCN global status |
| `referenceID`          | `dc:source` | source reference |
| `remarks`              | `dwc:occurrenceRemarks` | passthrough |

### Supported gazetteers for `areaID`

`ISO` (2‑letter country), `TDWG`/`WGSRPD`, `TEOW`, `LONGHURST`, `FAO`, `REALM`, `IHO`,
`MRGID`, and `TEXT` (free text). Use the CURIE form `GAZETTEER:code` (e.g. `ISO:US`,
`TDWG:GER`) or the `gazetteer` + `areaID` columns. See
<https://catalogueoflife.github.io/col-gazetteers/>.

### Configuration keys (`RelatedInfoConfig`)

| Setting | Default | Meaning |
|---------|---------|---------|
| `related.iucn` | `19491596-35ae-4a91-9a98-85cf505f1bd3` | IUCN Red List dataset (GBIF UUID) |
| `related.citesI` | `314512` | CITES Appendix I dataset (CLB key) |
| `related.citesII` | `314531` | CITES Appendix II dataset (CLB key) |
| `related.citesIII` | `314533` | CITES Appendix III dataset (CLB key) |
| `related.griisPublisherKey` | `cdef28b1-db4e-4c58-aa71-3c5238c2d0b5` | GRIIS GBIF publisher key |

---

## 6. Checklist before publishing

- [ ] Every record has a clean `scientificName`, `authorship`, and `rank` (so the names index
      can match).
- [ ] **IUCN**: a `Distribution` with `area = Global` and a valid `threatStatus`.
- [ ] **CITES**: the species is present in the correct appendix dataset; a `link` is provided.
- [ ] **GRIIS**: dataset metadata has a `country_XX` keyword.
- [ ] **GRIIS**: introduced distributions use `establishmentMeans = introduced`.
- [ ] **GRIIS**: invasive species carry an `isInvasive = true` property (ColDP `TaxonProperty`
      or DwC Species Profile extension).
- [ ] The dataset is **not private** in ChecklistBank.
