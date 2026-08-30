package org.gbif.taxon.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.Dataset;
import life.catalogue.db.mapper.DatasetMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.api.Checklist;
import org.gbif.taxon.config.ChecklistConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Exposes the taxonomies this API serves, resolving each configured GBIF dataset key to the
 * ChecklistBank dataset that currently backs it.
 */
@Service
public class ChecklistDao {
  private static final Logger LOG = LoggerFactory.getLogger(ChecklistDao.class);
  private static final String COLDP = "ColDP";
  private static final String DWCA = "DwCA";

  private final DatasetKeyMap keyMap;
  private final SqlSessionFactory factory;
  private final ChecklistConfig cfg;

  /**
   * Keyed by the ChecklistBank key, not the GBIF one, so a new COL release simply loads a new
   * entry instead of requiring the cache to be invalidated.
   */
  private final LoadingCache<Integer, Checklist> cache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofDays(1))
    .build(this::load);

  public ChecklistDao(DatasetKeyMap keyMap, SqlSessionFactory factory, ChecklistConfig cfg) {
    this.keyMap = keyMap;
    this.factory = factory;
    this.cfg = cfg;
  }

  /**
   * @return all configured checklists in their configured order, skipping any that cannot be resolved
   */
  public List<Checklist> list() {
    List<Checklist> checklists = new ArrayList<>();
    for (UUID key : cfg.getKeys()) {
      try {
        checklists.add(get(key));
      } catch (NotFoundException | MissingGBIFKeyException e) {
        LOG.warn("Configured checklist {} cannot be resolved in ChecklistBank, skipping it", key, e);
      }
    }
    return checklists;
  }

  public Checklist get(UUID datasetKey) {
    return cache.get(keyMap.toCLB(datasetKey));
  }

  public void flushCache() {
    cache.invalidateAll();
  }

  private Checklist load(Integer clbKey) {
    try (var session = factory.openSession()) {
      Dataset d = session.getMapper(DatasetMapper.class).get(clbKey);
      if (d == null) {
        throw NotFoundException.notFound(Checklist.class, clbKey);
      }
      return convert(d);
    }
  }

  private Checklist convert(Dataset d) {
    var c = new Checklist();
    c.setKey(keyMap.toGBIF(d.getKey()));
    c.setChecklistBankKey(d.getKey());
    c.setAlias(d.getAlias());
    c.setTitle(d.getTitle());
    c.setVersion(d.getVersion());
    if (d.getIssued() != null) {
      c.setIssued(d.getIssued().toString());
    }
    if (d.getDoi() != null) {
      c.setDoi(d.getDoi().getDoiName());
    }
    if (d.getVersionDoi() != null) {
      c.setVersionDoi(d.getVersionDoi().getDoiName());
    }
    c.setUrl(d.getUrl());
    c.setLogo(d.getLogo());
    c.setUsageCount(d.getSize());
    c.setChecklistBankUrl(cfg.checklistBankUrl(d.getKey()));
    c.setColdpDownloadUrl(cfg.downloadUrl(d.getKey(), COLDP));
    c.setDwcaDownloadUrl(cfg.downloadUrl(d.getKey(), DWCA));
    return c;
  }
}
