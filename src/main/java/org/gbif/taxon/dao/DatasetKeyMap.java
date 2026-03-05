package org.gbif.taxon.dao;

import org.gbif.api.model.Constants;


import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;

import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.DSID;
import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.Datasets;
import life.catalogue.cache.LatestDatasetKeyCache;
import life.catalogue.dao.DatasetInfoCache;
import life.catalogue.db.mapper.DatasetMapper;

@Component
public class DatasetKeyMap {
  private static final Logger LOG = LoggerFactory.getLogger(DatasetKeyMap.class);
  public static final UUID COL_BR_DATASET_KEY = UUID.fromString("e007cc4a-8704-449d-8829-bb209d26d6c8");
  private final SqlSessionFactory factory;
  private final LatestDatasetKeyCache cache;
  private final JsonFetcher  jsonFetcher;
  private final String matchingMetadata;
  private int colkey; // we load the COL key eagerly and keep it also outside of the expiring coffeine cache

  public DatasetKeyMap(SqlSessionFactory factory, LatestDatasetKeyCache cache, JsonFetcher  jsonFetcher,
                       @Value("${matching.url}") String matchingMetadata
  ) throws IOException {
    this.factory = factory;
    this.cache = cache;
    this.jsonFetcher = jsonFetcher;
    this.matchingMetadata = matchingMetadata;
    colkey = retrieveCurrentColXRKey();
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
              key = info.origin == DatasetOrigin.XRELEASE ? Constants.COL_DATASET_KEY : COL_BR_DATASET_KEY;
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

  public UUID toGBIF(int datasetKey) {
    var dk = clb2gbif.get(datasetKey);
    if (dk == null) {
      throw new IllegalArgumentException("Unknown dataset key: " + datasetKey);
    }
    return dk;
  }

  @VisibleForTesting
  protected int retrieveCurrentColXRKey() {
    int key = 0;
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

  public int toCLB(UUID datasetKey) {
    Integer dk;
    if (Constants.COL_DATASET_KEY.equals(datasetKey)) {
      dk = colkey;
    } else if (COL_BR_DATASET_KEY.equals(datasetKey)) {
      // we map the base release to the latest published version
      dk = cache.getLatestRelease(Datasets.COL, false);
    } else {
      dk = gbif2clb.get(datasetKey);
    }

    if (dk == null || dk < 0) {
      throw new IllegalArgumentException("Unknown dataset key: " + datasetKey);
    } else {
      // we cache the reverse mapping as mapping COL releases to the GBIF UUIDs via the db is difficult otherwise
      clb2gbif.put(dk, datasetKey);
    }
    return dk;
  }

  public DSID<String> toDSID(UUID datasetKey, String key) {
    return DSID.of(toCLB(datasetKey), key);
  }

  public void flush() throws IOException {
    LOG.info("Flushing dataset key map. Current COL key: " + colkey);
    colkey = retrieveCurrentColXRKey();
    gbif2clb.invalidateAll();
    clb2gbif.invalidateAll();
    cache.clear();
    LOG.info("Flushed dataset key map. New COL key: " + colkey);
  }
}
