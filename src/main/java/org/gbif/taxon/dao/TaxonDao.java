package org.gbif.taxon.dao;

import life.catalogue.api.model.Page;
import life.catalogue.api.model.ResultPage;
import life.catalogue.dao.MetricsDao;
import life.catalogue.dao.NameDao;
import life.catalogue.dao.TreeDao;
import life.catalogue.db.mapper.NameUsageMapper;

import life.catalogue.es.NameUsageIndexService;
import life.catalogue.img.ThumborConfig;
import life.catalogue.img.ThumborService;
import life.catalogue.matching.nidx.NameIndexFactory;
import life.catalogue.printer.JsonTreePrinter;

import org.apache.ibatis.session.SqlSessionFactory;


import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.taxon.api.SimpleUsage;
import org.gbif.taxon.api.TreeUsage;
import org.gbif.taxon.api.UsageInfo;


import java.io.Writer;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TaxonDao {
  private DatasetKeyMap map;
  private ApiConverter converter;
  private SqlSessionFactory factory;
  private TreeDao treeDao;
  private life.catalogue.dao.TaxonDao taxonDao;

  public TaxonDao(DatasetKeyMap map, ApiConverter converter, SqlSessionFactory factory) {
    this.map = map;
    this.converter = converter;
    this.factory = factory;
    this.treeDao = new TreeDao(factory);
    var indexService = NameUsageIndexService.passThru();
    MetricsDao mdao = new MetricsDao(factory);
    NameDao ndao = new NameDao(factory, indexService, NameIndexFactory.passThru(), null);
    this.taxonDao = new life.catalogue.dao.TaxonDao(factory, ndao, mdao, new ThumborService(new ThumborConfig()), indexService, null, null);
  }

  public SimpleUsage get(UUID uuid, String taxonKey) {
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      return converter.convert(num.getSimple(map.toDSID(uuid, taxonKey)));
    }
  }

  public UsageInfo getInfo(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      var usage = num.get(dsid);
      if (usage == null) return null;

      var clbInfo = new life.catalogue.api.model.UsageInfo(usage);
      taxonDao.fillUsageInfo(session, clbInfo, null, true, false, true, true, true, true, false, false, true, true, false, false, false, false, false);
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

  private static Page page(Pageable p) {
    return new Page((int)p.getOffset(), p.getLimit());
  }
  private static <T, X> PagingResponse<T> resp(ResultPage<X> rp, Function<X, T> converter) {
    return new PagingResponse<>(rp.getOffset(), rp.getLimit(), (long) rp.getTotal(),
      rp.getResult().stream().map(converter).toList()
    );
  }

  public PagingResponse<TreeUsage> root(UUID uuid, Pageable page) {
    int datasetKey = map.toCLB(uuid);
    var resp = treeDao.root(datasetKey, -1, false, true, null, page(page));
    return resp(resp, converter::convertTree);
  }

  /**
   * @return classification starting with the given start id
   */
  public List<TreeUsage> classification(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    var nodes = treeDao.classification(dsid, -1, true, false, null, null);
    if (nodes == null || nodes.size() <= 1) return List.of();
    return nodes.stream()
      .map(converter::convertTree)
      .collect(Collectors.toList());
  }

  public PagingResponse<TreeUsage> children(UUID uuid, String taxonKey, Pageable page) {
    var dsid = map.toDSID(uuid, taxonKey);
    var resp = treeDao.children(dsid, -1, false, true, null, page(page));
    return resp(resp, converter::convertTree);
  }
}
