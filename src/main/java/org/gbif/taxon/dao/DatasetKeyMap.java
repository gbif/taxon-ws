package org.gbif.taxon.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.DSID;
import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.Datasets;
import life.catalogue.cache.LatestDatasetKeyCache;
import life.catalogue.dao.DatasetInfoCache;
import life.catalogue.db.mapper.DatasetMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.config.ColConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class DatasetKeyMap {
  private static final Logger LOG = LoggerFactory.getLogger(DatasetKeyMap.class);
  private final SqlSessionFactory factory;
  private final LatestDatasetKeyCache cache;
  private final JsonFetcher  jsonFetcher;
  private final String matchingMetadata;
  private final ColConfig cfg;
  private final boolean fixedColKey;
  private int colkey; // we load the COL key eagerly and keep it also outside of the expiring coffeine cache

  public DatasetKeyMap(SqlSessionFactory factory, LatestDatasetKeyCache cache, JsonFetcher  jsonFetcher, ColConfig cfg) throws IOException {
    this.cfg = cfg;
    this.factory = factory;
    this.cache = cache;
    this.jsonFetcher = jsonFetcher;
    this.matchingMetadata = cfg.getMatchingUrl();
    this.fixedColKey = cfg.getDatasetKey() != null;
    colkey = this.fixedColKey ? cfg.getDatasetKey() : retrieveCurrentColXRKey();
    LOG.info("Using COL XR key {}", colkey);
  }

  private final LoadingCache<UUID, Integer> gbif2clb = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofDays(7))
    .build(this::lookupByGbif);

  private final LoadingCache<Integer, UUID> clb2gbif = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofDays(7))
    .build(this::lookupByClb);

  private Integer lookupByGbif(UUID uuid) {
    try (var session = factory.openSession()) {
      Integer dkey = session.getMapper(DatasetMapper.class).getKeyByGBIF(uuid);
      if (dkey != null) {
        clb2gbif.put(dkey, uuid);
      }
      return dkey;
    }
  }

  private UUID lookupByClb(Integer datasetKey) {
    try (var session = factory.openSession()) {
      var dataset = session.getMapper(DatasetMapper.class).get(datasetKey);
      if (dataset != null) {
        var key = dataset.getGbifKey();
        if (key == null) {
          // try COL releases which are not mapped to a GBIF key in the dataset table
          try {
            var info = DatasetInfoCache.CACHE.info(datasetKey, true);
            if (info != null && info.sourceKey != null && info.sourceKey == Datasets.COL) {
              key = info.origin == DatasetOrigin.XRELEASE ? cfg.getExtendedRelease() : cfg.getBaseRelease();
            }
          } catch (NotFoundException e) {
            return null;
          }
        }
        if (key != null) {
          gbif2clb.put(key, datasetKey);
          return key;
        }
      }
      return null;
    }
  }

  public UUID toGBIF(int datasetKey) throws MissingGBIFKeyException {
    var dk = clb2gbif.get(datasetKey);
    if (dk == null) {
      throw new MissingGBIFKeyException(datasetKey);
    }
    return dk;
  }

  @VisibleForTesting
  protected int retrieveCurrentColXRKey() {
    if (StringUtils.isBlank(matchingMetadata)) {
      throw new IllegalStateException("Missing matching URL configuration. Please configure 'col.matchingUrl'");
    }
    int key;
    try {
      JsonNode json = jsonFetcher.fetchJson(matchingMetadata);
      key = json
        .path("mainIndex")
        .path("clbDatasetKey")
        .asInt(-1);
      LOG.info("Retrieved current COL XR key {}", key);
    } catch (Exception e) {
      LOG.error("Failed to retrieve current COL XR key from matcher-ws located at {}", matchingMetadata);
      // rethrow
      throw e;
    }
    return key;
  }

  public int getColKey() {
    return colkey;
  }

  /**
   * Re-reads the currently live COL XR key from the matching service.
   * Does nothing if the key is fixed via configuration, e.g. on the dev environment.
   *
   * @return true if the key has changed
   */
  public boolean refreshColKey() {
    if (fixedColKey) {
      return false;
    }
    int newKey = retrieveCurrentColXRKey();
    if (newKey == colkey) {
      return false;
    }
    LOG.info("COL XR key changed from {} to {}", colkey, newKey);
    colkey = newKey;
    return true;
  }

  public int toCLB(UUID datasetKey) {
    Integer dk;
    if (cfg.getExtendedRelease().equals(datasetKey)) {
      dk = colkey;
    } else if (cfg.getBaseRelease().equals(datasetKey)) {
      // we map the base release to the latest published version
      dk = cache.getLatestRelease(Datasets.COL, false);
    } else {
      dk = gbif2clb.get(datasetKey);
    }

    if (dk == null || dk < 0) {
      throw new NotFoundException(datasetKey, "Unknown dataset key: " + datasetKey);
    } else {
      // we cache the reverse mapping as mapping COL releases to the GBIF UUIDs via the db is difficult otherwise
      clb2gbif.put(dk, datasetKey);
    }
    return dk;
  }

  public DSID<String> toDSID(UUID datasetKey, String key) {
    return DSID.of(toCLB(datasetKey), key);
  }

  /**
   * @return the currently cached dataset key map
   */
  public Map<UUID, Integer> bimap() throws IOException {
    BiMap<UUID, Integer> map = HashBiMap.create(gbif2clb.asMap());
    return map;
  }

  public void flush() throws IOException {
    LOG.info("Flushing dataset key map. Current COL key: " + colkey);
    refreshColKey();
    gbif2clb.invalidateAll();
    clb2gbif.invalidateAll();
    cache.clear();
    LOG.info("Flushed dataset key map. New COL key: " + colkey);
  }
}
