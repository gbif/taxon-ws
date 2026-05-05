import glob
import gzip
import os
import random
import re

from locust import HttpUser, between, task, tag

DATASET_KEY = "7ddf754f-d193-4cc9-b351-99906754a03b"
SITEMAPS_DIR = os.environ.get("SITEMAPS_DIR", os.path.expanduser("~/code/col/portal/sitemaps"))
MAX_IDS = int(os.environ.get("MAX_IDS", "1000000"))

SEARCH_TERMS = [
    "Homo sapiens",
    "Pinus sylvestris",
    "Canis lupus",
    "Rosa canina",
    "Quercus robur",
    "Insecta",
    "Fungi",
    "Aves",
    "Plantae",
    "Felis catus",
    "Panthera leo",
    "Arabidopsis thaliana",
    "Drosophila melanogaster",
    "Poa annua",
    "Abies alba",
]

SUGGEST_PREFIXES = [
    "Hom", "Pin", "Can", "Ros", "Que",
    "Ave", "Ins", "Fun", "Fel", "Pan",
    "Ara", "Dro", "Mag", "Sal", "Poa"
]


def _numeric_key(path: str) -> int:
    m = re.search(r"\d+", os.path.basename(path))
    return int(m.group()) if m else 0


def load_taxon_ids() -> list[str]:
    files = sorted(glob.glob(os.path.join(SITEMAPS_DIR, "sitemap-*.txt.gz")), key=_numeric_key)
    ids: list[str] = []
    files_read = 0
    for path in files:
        if len(ids) >= MAX_IDS:
            break
        with gzip.open(path, "rt") as fh:
            for line in fh:
                url = line.strip()
                if url:
                    ids.append(url.rsplit("/", 1)[-1])
                    if len(ids) >= MAX_IDS:
                        break
        files_read += 1
    if not ids:
        raise RuntimeError(f"No taxon IDs loaded from {SITEMAPS_DIR!r} — check SITEMAPS_DIR")
    print(f"Loaded {len(ids)} taxon IDs from {files_read} sitemap file(s)")
    return ids


TAXON_IDS: list[str] = load_taxon_ids()


class TaxonUser(HttpUser):
    weight = 3
    wait_time = between(0.2, 2.0)

    @tag('taxon')
    @task(10)
    def get_taxon(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/{DATASET_KEY}/{tk}",
            name="/taxon/[datasetKey]/[taxonKey]",
        )

    @tag('info')
    @task(50)
    def get_taxon_info(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/{DATASET_KEY}/{tk}/info",
            name="/taxon/[datasetKey]/[taxonKey]/info",
        )

    @tag('breakdown')
    @task(5)
    def get_breakdown(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/{DATASET_KEY}/{tk}/breakdown",
            name="/taxon/[datasetKey]/[taxonKey]/breakdown",
        )

    @tag('related')
    @task(10)
    def get_related(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/{DATASET_KEY}/{tk}/related",
            name="/taxon/[datasetKey]/[taxonKey]/related",
        )

    @tag('search')
    @task(20)
    def search(self):
        self.client.get(
            f"/taxon/search/{DATASET_KEY}",
            params={"q": random.choice(SEARCH_TERMS)},
            name="/taxon/search/[datasetKey]",
        )

    @tag('search')
    @task(30)
    def suggest(self):
        self.client.get(
            f"/taxon/suggest/{DATASET_KEY}",
            params={"q": random.choice(SUGGEST_PREFIXES)},
            name="/taxon/suggest/[datasetKey]",
        )


class TreeUser(HttpUser):
    weight = 1
    wait_time = between(1.0, 3.0)

    @tag('tree')
    @task(10)
    def get_root(self):
        self.client.get(
            f"/taxon/tree/{DATASET_KEY}",
            name="/taxon/tree/[datasetKey]",
        )

    @tag('tree')
    @task(10)
    def get_classification(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/tree/{DATASET_KEY}/{tk}",
            name="/taxon/tree/[datasetKey]/[taxonKey]",
        )

    @tag('tree')
    @task(50)
    def get_children(self):
        tk = random.choice(TAXON_IDS)
        self.client.get(
            f"/taxon/tree/{DATASET_KEY}/{tk}/children",
            name="/taxon/tree/[datasetKey]/[taxonKey]/children",
        )
