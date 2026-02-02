package org.gbif.species.dao;

import life.catalogue.api.model.DSID;
import life.catalogue.api.model.NameUsageBase;
import life.catalogue.api.model.Page;
import life.catalogue.api.model.TreeNode;
import life.catalogue.dao.MetricsDao;
import life.catalogue.dao.NameDao;
import life.catalogue.dao.TaxonDao;
import life.catalogue.dao.TreeDao;
import life.catalogue.db.mapper.DistributionMapper;
import life.catalogue.db.mapper.MediaMapper;
import life.catalogue.db.mapper.NameUsageMapper;
import life.catalogue.db.mapper.ReferenceMapper;
import life.catalogue.db.mapper.SynonymMapper;
import life.catalogue.db.mapper.TaxonPropertyMapper;
import life.catalogue.db.mapper.VernacularNameMapper;

import life.catalogue.es.NameUsageIndexService;
import life.catalogue.img.ThumborConfig;
import life.catalogue.img.ThumborService;
import life.catalogue.matching.nidx.NameIndexFactory;
import life.catalogue.printer.JsonTreePrinter;

import org.apache.ibatis.session.SqlSessionFactory;

import org.gbif.species.api.NameUsage;
import org.gbif.species.api.SimpleUsage;
import org.gbif.species.api.TreeUsage;
import org.gbif.species.api.UsageInfo;


import java.io.Writer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SpeciesDao {
  private DatasetKeyMap map;
  private ApiConverter converter;
  private SqlSessionFactory factory;
  private TreeDao treeDao;
  private TaxonDao taxonDao;

  public SpeciesDao(DatasetKeyMap map, ApiConverter converter, SqlSessionFactory factory) {
    this.map = map;
    this.converter = converter;
    this.factory = factory;
    this.treeDao = new TreeDao(factory);
    var indexService = NameUsageIndexService.passThru();
    MetricsDao mdao = new MetricsDao(factory);
    NameDao ndao = new NameDao(factory, indexService, NameIndexFactory.passThru(), null);
    this.taxonDao = new TaxonDao(factory, ndao, mdao, new ThumborService(new ThumborConfig()), indexService, null, null);
  }

  public NameUsage get(UUID uuid, String taxonKey) {
    return converter.convert(getCLB(uuid, taxonKey));
  }

  public NameUsageBase getCLB(UUID uuid, String taxonKey) {
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      return num.get(map.toDSID(uuid, taxonKey));
    }
  }

  public UsageInfo getInfo(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      var usage = num.get(dsid);
      if (usage == null) return null;

      var clbInfo = new life.catalogue.api.model.UsageInfo(usage);

      if (!usage.getStatus().isSynonym()) {
        clbInfo.setVernacularNames(session.getMapper(VernacularNameMapper.class).listByTaxon(dsid));
        clbInfo.setDistributions(session.getMapper(DistributionMapper.class).listByTaxon(dsid));
        clbInfo.setMedia(session.getMapper(MediaMapper.class).listByTaxon(dsid));
        clbInfo.setProperties(session.getMapper(TaxonPropertyMapper.class).listByTaxon(dsid));
      }

      // synonyms
      var synonyms = session.getMapper(SynonymMapper.class).listByTaxon(dsid);
      if (synonyms != null && !synonyms.isEmpty()) {
        var synonymy = new life.catalogue.api.model.Synonymy();
        synonymy.getHeterotypic().addAll(synonyms);
        clbInfo.setSynonyms(synonymy);
      }

      // published-in reference
      if (usage.getName().getPublishedInId() != null) {
        var refMapper = session.getMapper(ReferenceMapper.class);
        clbInfo.setPublishedIn(refMapper.get(DSID.of(dsid.getDatasetKey(), usage.getName().getPublishedInId())));
      }

      // collect all referenced reference IDs and load them
      var refIds = new java.util.HashSet<String>();
      if (usage.getAccordingToId() != null) {
        refIds.add(usage.getAccordingToId());
      }
      if (!refIds.isEmpty()) {
        var refMapper = session.getMapper(ReferenceMapper.class);
        var refs = refMapper.listByIds(dsid.getDatasetKey(), refIds);
        if (refs != null) {
          for (var ref : refs) {
            clbInfo.addReference(ref);
          }
        }
      }

      return converter.convert(clbInfo);
    }
  }

  public JsonTreePrinter childrenBreakdownPrinter(UUID uuid, String id, Writer writer) {
    int datasetKey = map.toCLB(uuid);
    return taxonDao.childrenBreakdownPrinter(datasetKey, id, writer);
  }

  public List<SimpleUsage> getRelated(UUID uuid, String taxonKey, String type) {
    // TODO: cross-dataset related usages deferred
    return List.of();
  }

  public List<TreeUsage> root(UUID uuid) {
    int datasetKey = map.toCLB(uuid);
    return treeDao.root(datasetKey, datasetKey, false, true, TreeNode.Type.SOURCE, new Page(0, 10000))
      .getResult().stream()
      .map(converter::convertTree)
      .collect(Collectors.toList());
  }

  public TreeUsage getSimple(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    int datasetKey = dsid.getDatasetKey();
    var nodes = treeDao.classification(dsid, datasetKey, false, true, TreeNode.Type.SOURCE, null);
    if (nodes == null || nodes.isEmpty()) return null;
    return converter.convertTree(nodes.get(0));
  }

  public List<TreeUsage> parents(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    int datasetKey = dsid.getDatasetKey();
    var nodes = treeDao.classification(dsid, datasetKey, false, true, TreeNode.Type.SOURCE, null);
    if (nodes == null || nodes.size() <= 1) return List.of();
    // classification includes the target node as the first element; skip it
    return nodes.subList(1, nodes.size()).stream()
      .map(converter::convertTree)
      .collect(Collectors.toList());
  }

  public List<TreeUsage> children(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    int datasetKey = dsid.getDatasetKey();
    return treeDao.children(dsid, datasetKey, false, true, TreeNode.Type.SOURCE, new Page(0, 10000))
      .getResult().stream()
      .map(converter::convertTree)
      .collect(Collectors.toList());
  }
}
