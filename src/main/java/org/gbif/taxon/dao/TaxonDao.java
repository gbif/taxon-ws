package org.gbif.taxon.dao;

import life.catalogue.api.model.DSID;
import life.catalogue.api.model.DatasetImport;
import life.catalogue.api.model.SimpleName;
import life.catalogue.api.model.SimpleNameInDataset;
import life.catalogue.api.vocab.AreaImpl;
import life.catalogue.api.vocab.Country;
import life.catalogue.api.vocab.Gazetteer;
import life.catalogue.api.vocab.LonghurstArea;
import life.catalogue.api.vocab.TdwgArea;
import life.catalogue.dao.DatasetImportDao;
import life.catalogue.dao.DatasetInfoCache;
import life.catalogue.db.mapper.DatasetImportMapper;
import life.catalogue.db.mapper.DistributionMapper;
import life.catalogue.es.indexing.NameUsageIndexService;
import life.catalogue.es.search.NameUsageSearchService;


import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.RelatedInfo;
import org.gbif.taxon.api.TreeUsage;
import org.gbif.taxon.api.UsageInfo;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchResult;
import org.gbif.taxon.api.search.NameUsageSuggestRequest;
import org.gbif.taxon.api.search.NameUsageSuggestResult;


import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.ibatis.session.SqlSessionFactory;


import org.gbif.taxon.config.DatasetKeys;


import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import life.catalogue.api.model.Page;
import life.catalogue.api.model.ResultPage;
import life.catalogue.api.vocab.DatasetType;
import life.catalogue.dao.MetricsDao;
import life.catalogue.dao.NameDao;
import life.catalogue.dao.TreeDao;
import life.catalogue.db.mapper.NameUsageMapper;
import life.catalogue.img.ThumborConfig;
import life.catalogue.img.ThumborService;
import life.catalogue.matching.nidx.NameIndexFactory;
import life.catalogue.printer.JsonTreePrinter;

@Service
public class TaxonDao {
  private final DatasetKeyMap map;
  private final ApiConverter converter;
  private final SqlSessionFactory factory;
  private final TreeDao treeDao;
  private final DatasetImportDao diDao;
  private final life.catalogue.dao.TaxonDao tDao;
  private final life.catalogue.es.suggest.NameUsageSuggestionService suggestionService;
  private final life.catalogue.es.search.NameUsageSearchService searchService;
  private final DatasetKeys datasetKeys;

  public TaxonDao(DatasetKeyMap map, ApiConverter converter, SqlSessionFactory factory, DatasetKeys datasetKeys,
                  NameUsageSearchService searchService,
                  life.catalogue.es.suggest.NameUsageSuggestionService suggestionService) {
    this.map = map;
    this.datasetKeys = datasetKeys;
    this.converter = converter;
    this.factory = factory;
    this.treeDao = new TreeDao(factory);
    var indexService = NameUsageIndexService.passThru();
    MetricsDao mdao = new MetricsDao(factory);
    NameDao ndao = new NameDao(factory, indexService, NameIndexFactory.passThru(), null);
    this.tDao = new life.catalogue.dao.TaxonDao(factory, ndao, mdao, new ThumborService(new ThumborConfig()), indexService, null, null);
    this.searchService = searchService;
    this.suggestionService = suggestionService;
    this.diDao = new DatasetImportDao(factory, null);
  }

  public ChecklistMetrics metrics(UUID uuid) {
    try (var session = factory.openSession()) {
      var dim = session.getMapper(DatasetImportMapper.class);
      int datasetKey = map.toCLB(uuid);
      var info = DatasetInfoCache.CACHE.info(datasetKey);
      DatasetImport imp;
      if (info.origin.isRelease()) {
        imp = diDao.getReleaseAttempt(datasetKey);
      } else {
        imp = dim.current(datasetKey);
      }
      return imp == null ? null : converter.convert(imp);
    }
  }

  public NameUsageSimple get(UUID uuid, String taxonKey) {
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
      tDao.fillUsageInfo(session, clbInfo, null, true, false, true, true, true, true, false, false, true, true, false, false, false, false, false);
      return converter.convert(clbInfo);
    }
  }

  public JsonTreePrinter childrenBreakdownPrinter(UUID uuid, String id, Writer writer) {
    int datasetKey = map.toCLB(uuid);
    return tDao.childrenBreakdownPrinter(datasetKey, id, writer);
  }

  public List<NameUsageSimple> listRelated(UUID uuid, String taxonKey,
                                           @Nullable Collection<DatasetType> datasetTypes,
                                           @Nullable Collection<UUID> datasetKeys,
                                           @Nullable Collection<UUID> publisherKeys) {
    Set<Integer> datasetIntKeys = new HashSet<>();;
    if (datasetKeys != null) {
      for (UUID key : datasetKeys) {
        datasetIntKeys.add(map.toCLB(key));
      }
    }
    return listRelatedCLB(uuid, taxonKey, datasetTypes, datasetIntKeys, publisherKeys)
      .stream().map(converter::convert).toList();
  }

  private List<SimpleNameInDataset> listRelatedCLB(UUID uuid, String taxonKey,
                                                @Nullable Collection<DatasetType> datasetTypes,
                                                @Nullable Collection<Integer> datasetKeys,
                                                @Nullable Collection<UUID> publisherKeys) {
    int datasetKey = map.toCLB(uuid);
    Set<Integer> colKeys = Set.of(map.getColKey());
    return tDao.related(datasetKey, taxonKey, true, colKeys, null, datasetTypes, datasetKeys, publisherKeys);
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

  public SearchResponse<NameUsageSearchResult, NameUsageSearchParameter> search(NameUsageSearchRequest request) {
    return converter.convert(searchService.search(converter.convert(request), page(request)));
  }

  public List<NameUsageSuggestResult> suggest(NameUsageSuggestRequest request) {
    return suggestionService.suggest(converter.convert(request)).stream()
      .map(converter::convert)
      .toList();
  }

  private static Page page(SearchRequest<NameUsageSearchParameter> req) {
    return new Page((int)req.getOffset(), req.getLimit());
  }


  public RelatedInfo listRelatedInfo(UUID datasetKey, String taxonKey) {
    final var relInfo = new RelatedInfo();
    int datasetKeyCLB = map.toCLB(datasetKey);
    if (datasetKeys.getIucn() != null) {
      int datasetKeyIucn = map.toCLB(datasetKeys.getIucn());
      var iucn = findSingleRelated(datasetKeyCLB, taxonKey, datasetKeyIucn);
      if (iucn != null) {
        try (var session = factory.openSession()) {
          DistributionMapper dim = session.getMapper(DistributionMapper.class);
          var dists = dim.listByTaxon(iucn.toDSID(datasetKeyIucn));
          // find global entry
          for (var d : dists) {
            if (d.getArea().getName().equalsIgnoreCase("Global") && d.getThreatStatus() != null) {
              relInfo.setThreatStatus(d.getThreatStatus().name());
              relInfo.setThreatStatusUsage(converter.convert(iucn));
              break;
            }
          }
        }
      }
    }

    List<String> appendices = new ArrayList<>();
    if (datasetKeys.getCitesI() != null) {
      var rel = findSingleRelated(datasetKeyCLB, taxonKey, datasetKeys.getCitesI());
      if (rel != null) {
        appendices.add("I");
      }
    }
    if (datasetKeys.getCitesII() != null) {
      var rel = findSingleRelated(datasetKeyCLB, taxonKey, datasetKeys.getCitesII());
      if (rel != null) {
        appendices.add("II");
      }
    }
    relInfo.setCitesAppendix(appendices);
    return relInfo;
  }

  private SimpleNameInDataset findSingleRelated(Integer datasetKey, String taxonKey, Integer targetDatasetKey) {
    var list = tDao.related(datasetKey, taxonKey, false, null, null, null, Set.of(targetDatasetKey), null);
    if (list != null && !list.isEmpty()) {
      return list.getFirst();
    }
    return null;
  }
}
