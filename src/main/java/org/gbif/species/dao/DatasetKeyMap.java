package org.gbif.species.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import life.catalogue.api.model.DSID;

import life.catalogue.db.mapper.DatasetMapper;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatasetKeyMap {
  private SqlSessionFactory factory;

  public DatasetKeyMap(SqlSessionFactory factory) {
    this.factory = factory;
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
    Integer dk = gbif2clb.get(datasetKey);
    if (dk == null) {
      throw new IllegalArgumentException("Unknown dataset key: " + datasetKey);
    }
    return dk;
  }

  public DSID<String> toDSID(UUID datasetKey, String key) {
    return DSID.of(toCLB(datasetKey), key);
  }
}
