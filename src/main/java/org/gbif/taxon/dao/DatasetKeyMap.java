package org.gbif.taxon.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.DSID;

import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.Datasets;
import life.catalogue.cache.LatestDatasetKeyCache;
import life.catalogue.dao.DatasetInfoCache;
import life.catalogue.db.mapper.DatasetMapper;

import org.apache.ibatis.session.SqlSessionFactory;


import org.gbif.api.model.Constants;


import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatasetKeyMap {
  public static final UUID COL_BR_DATASET_KEY = UUID.fromString("e007cc4a-8704-449d-8829-bb209d26d6c8");
  private SqlSessionFactory factory;
  private LatestDatasetKeyCache cache;

  public DatasetKeyMap(SqlSessionFactory factory, LatestDatasetKeyCache cache) {
    this.factory = factory;
    this.cache = cache;
  }

  private final LoadingCache<UUID, Integer> gbif2clb = Caffeine.newBuilder()
    .build(this::lookupByGbif);

  private final LoadingCache<Integer, UUID> clb2gbif = Caffeine.newBuilder()
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

  public int toCLB(UUID datasetKey) {
    Integer dk;
    if (Constants.COL_DATASET_KEY.equals(datasetKey)) {
      // we map the UUID to the latest XR
      dk = cache.getLatestRelease(Datasets.COL, true);
    } else if (COL_BR_DATASET_KEY.equals(datasetKey)) {
      // we map the UUID to the latest Base Release
      dk = cache.getLatestRelease(Datasets.COL, false);
    } else {
      dk = gbif2clb.get(datasetKey);
    }

    if (dk == null) {
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
}
