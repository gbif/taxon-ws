package org.gbif.taxon.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.exception.SynonymException;
import life.catalogue.api.model.*;
import life.catalogue.api.vocab.DatasetType;
import life.catalogue.api.vocab.EstablishmentMeans;
import life.catalogue.dao.*;
import life.catalogue.db.mapper.*;
import life.catalogue.es.indexing.NameUsageIndexService;
import life.catalogue.es.search.NameUsageSearchService;
import life.catalogue.img.ThumborConfig;
import life.catalogue.img.ThumborService;
import life.catalogue.matching.nidx.NameIndexFactory;
import life.catalogue.printer.JsonTreeCollector;
import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.taxon.api.*;
import org.gbif.taxon.api.Distribution;
import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.api.search.*;
import org.gbif.taxon.config.RelatedInfoConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TaxonDao {
  private final static Set<String> INVASIVE_TRUE_VALUES = Set.of("yes", "true", "1", "invasive", "isinvasive");
  private final DatasetKeyMap map;
  private final ApiConverter converter;
  private final SqlSessionFactory factory;
  private final TreeDao treeDao;
  private final DatasetImportDao diDao;
  private final life.catalogue.dao.TaxonDao tDao;
  private final life.catalogue.es.suggest.NameUsageSuggestionService suggestionService;
  private final life.catalogue.es.search.NameUsageSearchService searchService;
  private final RelatedInfoConfig relatedInfoConfig;
  private final Pattern countryCodePattern = Pattern.compile("country_([A-Z]+)", Pattern.CASE_INSENSITIVE);
  private final LoadingCache<Integer, String> countryCode = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofDays(7))
    .build(this::lookupDatasetCountry);

  private String lookupDatasetCountry(Integer datasetKey) {
    try (var session = factory.openSession()) {
      var dm = session.getMapper(DatasetMapper.class);
      var d = dm.get(datasetKey);
      if (d != null && d.getKeyword() != null) {
        for (var kw : d.getKeyword()) {
          var m = countryCodePattern.matcher(kw);
          if (m.find()) {
            return m.group(1).toUpperCase();
          }
        }
      }
    }
    return null;
  }

  public void flushCache() {
    countryCode.invalidateAll();
  }

  public TaxonDao(DatasetKeyMap map, ApiConverter converter, SqlSessionFactory factory, RelatedInfoConfig relatedInfoConfig,
                  NameUsageSearchService searchService,
                  life.catalogue.es.suggest.NameUsageSuggestionService suggestionService) {
    this.map = map;
    this.relatedInfoConfig = relatedInfoConfig;
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
      if (imp == null) {
        throw NotFoundException.notFound(ChecklistMetrics.class, uuid);
      }
      return converter.convert(imp);
    }
  }

  public NameUsageSimple get(UUID uuid, String taxonKey) {
    var key = map.toDSID(uuid, taxonKey);
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      var sn = num.getSimpleInDataset(key);
      return converter.convert(nonNull(sn, uuid, taxonKey));
    }
  }

  private static <T> T nonNull(T obj, UUID uuid, String taxonKey) {
    if (obj == null) {
      var msg = "Taxon " + uuid + "/" +  taxonKey + " does not exist";
      throw new NotFoundException(msg);
    }
    return obj;
  }

  public NameUsageInfo getInfo(UUID uuid, String taxonKey) {
    var dsid = map.toDSID(uuid, taxonKey);
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      var usage = num.get(dsid);
      if (usage == null) {
        throw NotFoundException.notFound(NameUsage.class, dsid);
      }

      var clbInfo = new life.catalogue.api.model.UsageInfo(usage);
      tDao.fillUsageInfo(session, clbInfo, null, true, false, true, true, true, true, false, false, true, true, false, false, false, false, false);
      var info = converter.convert(clbInfo);

      return info;
    }
  }

  public TaxonBreakdown childrenBreakdown(UUID uuid, String taxonID) {
    int datasetKey = map.toCLB(uuid);
    // this can throw NotFoundException
    var collector = tDao.childrenBreakdownCollector(datasetKey, taxonID);
    try {
      collector.print();
    } catch (IOException e) {
      // this should not be possible as the collector does not write to IO !!!
      // artefact of the abstract base printer class which is targeting real writers
      throw new RuntimeException(e);
    }

    TaxonBreakdown breakdown = new TaxonBreakdown();
    breakdown.setTaxonID(taxonID);
    breakdown.setTaxonRank(collector.getTaxon().getRank());
    breakdown.setScientificName(collector.getTaxon().getName());
    breakdown.setSpecies(countSpecies(datasetKey, taxonID));
    breakdown.setBreakdown(new ArrayList<>(collector.getRoot().size()));
    for (var n : collector.getRoot()) {
      var tb = tb(n);
      breakdown.getBreakdown().add(tb);
      if (!n.children.isEmpty()) {
        tb.setBreakdown(new ArrayList<>(n.children.size()));
        for (var n2 : n.children) {
          var tb2 = tb(n2);
          tb.getBreakdown().add(tb2);
        }
      }
    }
    return breakdown;
  }

  private int countSpecies(int datasetKey, String taxonID) {
    try (SqlSession session = factory.openSession()) {
      var tm = session.getMapper(TaxonMetricsMapper.class);
      var m = tm.get(DSID.of(datasetKey, taxonID));
      if (m != null) return m.getSpeciesCount();
    }
    return -1;
  }

  private static TaxonBreakdown tb(JsonTreeCollector.TreeName tn) {
    TaxonBreakdown tb = new TaxonBreakdown();
    tb.setTaxonID(tn.name.getId());
    tb.setTaxonRank(tn.name.getRank());
    tb.setScientificName(tn.name.getName());
    tb.setSpecies(tn.count);
    return tb;
  }

  @SneakyThrows
  public List<NameUsageSimple> listRelated(UUID uuid, String taxonKey,
                                           @Nullable Collection<DatasetType> datasetTypes,
                                           @Nullable Collection<UUID> datasetKeys,
                                           @Nullable Collection<UUID> publisherKeys) {
    final int dkey = map.toCLB(uuid);
    Set<Integer> datasetIntKeys = new HashSet<>();;
    if (datasetKeys != null) {
      for (UUID key : datasetKeys) {
        datasetIntKeys.add(map.toCLB(key));
      }
    }
    return listRelatedCLB(uuid, taxonKey, datasetTypes, datasetIntKeys, publisherKeys).stream()
      .filter(u -> u.getDatasetKey() != dkey)
      .map(converter::convert)
      .toList();
  }

  private List<SimpleNameInDataset> listRelatedCLB(UUID uuid, String taxonKey,
                                                @Nullable Collection<DatasetType> datasetTypes,
                                                @Nullable Collection<Integer> datasetKeys,
                                                @Nullable Collection<UUID> publisherKeys) {
    return tDao.related(map.toCLB(uuid), taxonKey, true, Set.of(map.getColKey()), null, datasetTypes, datasetKeys, publisherKeys);
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
    if (nodes == null || nodes.isEmpty()) {
      throw new NotFoundException(taxonKey, "No classification found for taxon " + taxonKey);
    }
    return nodes.stream()
      .map(converter::convertTree)
      .collect(Collectors.toList());
  }

  public PagingResponse<TreeUsage> children(UUID uuid, String taxonKey, Pageable page) {
    var dsid = map.toDSID(uuid, taxonKey);
    var resp = treeDao.children(dsid, -1, false, true, null, page(page));
    if (resp == null || resp.getResult().isEmpty()) {
      throwIfNotFound(dsid);
    }
    return resp(resp, converter::convertTree);
  }

  private void throwIfNotFound(DSID<String> dsid) {
    try (SqlSession session = factory.openSession()) {
      boolean exists = session.getMapper(NameUsageMapper.class).exists(dsid);
      if (!exists) {
        throw new NotFoundException(dsid.getId(), "Taxon not found: " + dsid.getId());
      }
    }
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

  private SimpleName taxonOr404(int datasetKeyCLB, String taxonID) {
    try (SqlSession session = factory.openSession()) {
      var key = DSID.of(datasetKeyCLB, taxonID);
      SimpleName sn = session.getMapper(NameUsageMapper.class).getSimple(key);
      if (sn == null) {
        throw NotFoundException.notFound(NameUsage.class, key);
      } else if (sn.isSynonym()) {
        throw new SynonymException(key, sn.getParent());
      }
      return sn;
    }
  }

  public RelatedInfo listRelatedInfo(UUID datasetKey, String taxonID) {
    final var relInfo = new RelatedInfo();
    int datasetKeyCLB = map.toCLB(datasetKey);

    var tax = taxonOr404(datasetKeyCLB, taxonID);
    // REDLIST
    if (relatedInfoConfig.getIucn() != null) {
      int datasetKeyIucn = map.toCLB(relatedInfoConfig.getIucn());
      var iucn = findSingleRelated(datasetKeyCLB, taxonID, datasetKeyIucn);
      if (iucn != null) {
        try (var session = factory.openSession()) {
          DistributionMapper dim = session.getMapper(DistributionMapper.class);
          var dists = dim.listByTaxon(iucn.toDSID(datasetKeyIucn));
          // find global entry
          for (var d : dists) {
            if (d.getArea().getName().equalsIgnoreCase("Global") && d.getThreatStatus() != null) {
              var red = converter.convert(iucn);
              red.setThreatStatus(d.getThreatStatus().name());
              relInfo.setRedlist(red);
              break;
            }
          }
        }
      }
    }

    // CITES
    addAppendix(relInfo, datasetKeyCLB, taxonID, "I", relatedInfoConfig.getCitesI());
    addAppendix(relInfo, datasetKeyCLB, taxonID, "II", relatedInfoConfig.getCitesII());
    addAppendix(relInfo, datasetKeyCLB, taxonID, "III", relatedInfoConfig.getCitesIII());

    // GRIIS
    // https://github.com/gbif/portal16/issues/883#issuecomment-784536216
    if (relatedInfoConfig.getGriisPublisherKey() != null) {
      var list = tDao.related(datasetKeyCLB, taxonID, true, null, null, null, null, Set.of(relatedInfoConfig.getGriisPublisherKey()));
      if (list != null) {
        var distributions = new ArrayList<Distribution>();
        try (var session = factory.openSession()) {
          var dm = session.getMapper(DistributionMapper.class);
          var tpm = session.getMapper(TaxonPropertyMapper.class);
          for (var rel : list) {
            UUID gbifKey = map.toGBIF(rel.getDatasetKey());
            var taxKey = rel.toDSID(rel.getDatasetKey());
            // lookup isInvasive from taxon properties first for the taxon.
            // this works because the dataset is restricted to one country only
            var props = tpm.listByTaxon(taxKey);
            Boolean invasive = null;
            if (props != null) {
              TaxonProperty invasiveTP = props.stream()
                .filter(tp -> tp.getProperty().equalsIgnoreCase(GbifTerm.isInvasive.name()))
                .findFirst().orElse(null);
              if (invasiveTP != null && invasiveTP.getValue() != null && INVASIVE_TRUE_VALUES.contains(invasiveTP.getValue().trim().toLowerCase())) {
                invasive = true;
              }
            }
            for (var d : dm.listByTaxon(taxKey)) {
              if (d.getEstablishmentMeans() == EstablishmentMeans.INTRODUCED && d.getArea() != null) {
                var griis = converter.convert(d);
                griis.setDatasetKey(gbifKey);
                griis.setTaxonID(rel.getId());
                // add additional country codes from dataset keyword metadata
                griis.setCountryCode(countryCode.get(rel.getDatasetKey()));
                griis.setIsInvasive(invasive);
                distributions.add(griis);
              }
            }
          }
        }
        if (!distributions.isEmpty()) {
          relInfo.setGriis(distributions);
        }
      }
    }
    return relInfo;
  }

  private void addAppendix(RelatedInfo relInfo, Integer datasetKey, String taxonID, String appendix, Integer citesDatasetKey) {
    if (citesDatasetKey != null) {
      var rel = findSingleRelated(datasetKey, taxonID, citesDatasetKey);
      if (rel != null) {
        var cite = converter.convert(rel);
        cite.setCitesAppendix(appendix);
        if (relInfo.getCites() == null) {
          relInfo.setCites(new ArrayList<>());
        }
        relInfo.getCites().add(cite);
      }
    }
  }

  private SimpleNameInDataset findSingleRelated(Integer datasetKey, String taxonKey, Integer targetDatasetKey) {
    var list = tDao.related(datasetKey, taxonKey, false, null, null, null, Set.of(targetDatasetKey), null);
    if (list != null && !list.isEmpty()) {
      return list.getFirst();
    }
    return null;
  }
}
