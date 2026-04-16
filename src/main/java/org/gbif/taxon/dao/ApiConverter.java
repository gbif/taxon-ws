package org.gbif.taxon.dao;

import life.catalogue.api.model.*;
import life.catalogue.api.search.NameUsageRequest;
import life.catalogue.api.search.NameUsageSearchResponse;
import life.catalogue.api.search.NameUsageSuggestion;
import life.catalogue.api.search.NameUsageWrapper;
import life.catalogue.api.vocab.Issue;
import life.catalogue.api.vocab.NomRelType;
import life.catalogue.api.vocab.area.Gazetteer;
import life.catalogue.parser.AreaParser;
import life.catalogue.parser.SafeParser;
import org.gbif.api.model.common.search.Facet;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.taxon.api.*;
import org.gbif.taxon.api.Distribution;
import org.gbif.taxon.api.Media;
import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.api.Reference;
import org.gbif.taxon.api.Synonymy;
import org.gbif.taxon.api.VernacularName;
import org.gbif.taxon.api.search.*;
import org.gbif.taxon.config.RelatedInfoConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiConverter {
  private static final String CLB_BASE_URL = "https://www.checklistbank.org/dataset/";

  /** Reverse map from CLB search parameter to the corresponding GBIF v2 parameter. */
  private static final Map<life.catalogue.api.search.NameUsageSearchParameter, NameUsageSearchParameter> CLB_TO_GBIF_PARAM;
  private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  static {
    CLB_TO_GBIF_PARAM = new EnumMap<>(life.catalogue.api.search.NameUsageSearchParameter.class);
    for (var p : NameUsageSearchParameter.values()) {
      CLB_TO_GBIF_PARAM.put(p.toClb(), p);
    }
  }

  private DatasetKeyMap map;
  private RelatedInfoConfig cfg;

  public ApiConverter(DatasetKeyMap map, RelatedInfoConfig cfg) {
    this.map = map;
    this.cfg = cfg;
  }

  private void copy(NameUsageBase from, NameUsageSimple to) {
    var name = from.getName();
    to.setDatasetKey(map.toGBIF(from.getDatasetKey()));
    to.setTaxonID(from.getId());
    if (from.getStatus().isSynonym()) {
      to.setAcceptedNameUsageID(from.getParentId());
    } else {
      to.setParentNameUsageID(from.getParentId());
    }
    to.setScientificName(name.getScientificName());
    to.setScientificNameAuthorship(name.getAuthorship());
    to.setTaxonRank(name.getRank());
    to.setTaxonomicStatus(from.getStatus());
    to.setNomenclaturalCode(str(name.getCode()));
    to.setReferences(from.getLink());
    if (from instanceof Taxon tax) {
      to.setExtinct(tax.isExtinct());
    }
    to.setLabel(from.getLabelHtml());
  }

  NameUsageSimple convert(NameUsageBase nub) {
    var sn = new NameUsageSimple();
    copy(nub, sn);
    return sn;
  }

  NameUsage convert(NameUsageBase nub, @Nullable Set<Issue> issues) {
    var nu = new NameUsage();
    var name = nub.getName();

    // SimpleUsage fields
    copy(nub, nu);

    // NameUsage-specific fields
    if (nub instanceof Synonym syn && syn.getAccepted() != null) {
      nu.setAcceptedNameUsage(syn.getAccepted().getLabel());
    }
    nu.setNameAccordingTo(nub.getAccordingTo());
    nu.setNamePhrase(nub.getNamePhrase());
    nu.setNomenclaturalStatus(str(name.getNomStatus()));
    nu.setNameType(name.getType());
    nu.setGenericName(name.getGenus());
    nu.setInfragenericEpithet(name.getInfragenericEpithet());
    nu.setSpecificEpithet(name.getSpecificEpithet());
    nu.setInfraspecificEpithet(name.getInfraspecificEpithet());
    nu.setCultivarEpithet(name.getCultivarEpithet());
    nu.setReferences(nub.getLink());
    nu.setTaxonRemarks(nub.getRemarks());
    if (issues != null && !issues.isEmpty()) {
      nu.setIssues(issues.stream().map(Issue::name).collect(Collectors.toList()));
    }
    return nu;
  }

  public NameUsageSimple convert(SimpleNameInDataset sn) {
    var su = convert((SimpleName)sn);
    // until we have CITES datasets in GBIF we can't map them
    if (cfg.isCites(sn.getDatasetKey())) {
      // TODO: remove in production
      su.setDatasetKey(NULL_UUID);
    } else {
      su.setDatasetKey(map.toGBIF(sn.getDatasetKey()));
    }
    if (sn.getLink() != null) {
      su.setReferences(sn.getLink());
    }
    return su;
  }

  /**
   * Only to be used for converting the synonym as all the synonyms point to the same taxon and parentID is very redundant.
   */
  private NameUsageSimple convert(Synonym s) {
    s.setParentId(null);
    return convert(new SimpleName(s));
  }

  private static void copy(SimpleName from, ClassificationUsage to) {
    to.setTaxonID(from.getId());
    to.setScientificName(from.getName());
    to.setScientificNameAuthorship(from.getAuthorship());
    to.setTaxonRank(from.getRank());
    to.setLabel(from.getLabelHtml());
  }

  public ClassificationUsage convert2classification(SimpleName sn) {
    var su = new ClassificationUsage();
    copy(sn, su);
    return su;
  }

  public NameUsageSimple convert(SimpleName sn) {
    var su = new NameUsageSimple();
    copy(sn, su);
    if (sn.getStatus() != null && sn.getStatus().isSynonym()) {
      su.setAcceptedNameUsageID(sn.getParentId());
    } else {
      su.setParentNameUsageID(sn.getParentId());
    }
    su.setTaxonomicStatus(sn.getStatus());
    su.setNomenclaturalCode(str(sn.getCode()));
    su.setExtinct(isTrue(sn.isExtinct()));

    return su;
  }

  private static Boolean isTrue(boolean b) {
    return b ? true : null;
  }

  TreeUsage convertTree(TreeNode tn) {
    var tu = new TreeUsage();

    tu.setTaxonID(tn.getId());
    if (tn.getStatus() != null && tn.getStatus().isSynonym()) {
      tu.setAcceptedNameUsageID(tn.getParentId());
    } else {
      tu.setParentNameUsageID(tn.getParentId());
    }
    tu.setScientificName(tn.getName());
    tu.setScientificNameAuthorship(tn.getAuthorship());
    tu.setTaxonRank(tn.getRank());
    tu.setTaxonomicStatus(tn.getStatus());
    tu.setLabel(tn.getLabelHtml());
    tu.setChildren(tn.getChildCount());
    tu.setSpecies(tn.getCount() != null ? tn.getCount() : 0);

    return tu;
  }

  Media convert(life.catalogue.api.model.Media m) {
    var media = new Media();
    media.setIdentifier(m.getUrl() != null ? m.getUrl().toString() : null);
    media.setType(str(m.getType()));
    media.setTitle(m.getTitle());
    media.setCreated(m.getCaptured() != null ? m.getCaptured().toString() : null);
    media.setCreator(m.getCapturedBy());
    media.setLicense(str(m.getLicense()));
    media.setReferences(m.getLink());
    media.setRemarks(m.getRemarks());
    return media;
  }

  VernacularName convert(life.catalogue.api.model.VernacularName vn) {
    var v = new VernacularName();
    v.setVernacularName(vn.getName());
    v.setLanguage(vn.getLanguage());
    v.setLocality(vn.getArea());
    v.setCountryCode(vn.getCountry() != null ? vn.getCountry().getIso2LetterCode() : null);
    v.setSex(str(vn.getSex()));
    v.setPreferredName(vn.isPreferred());
    v.setReferenceID(vn.getReferenceId());
    v.setRemarks(vn.getRemarks());
    return v;
  }

  Distribution convert(life.catalogue.api.model.Distribution d) {
    var dist = new Distribution();
    if (d.getArea() != null) {
      dist.setLocationID(d.getArea().getGlobalId());
      dist.setLocality(d.getArea().getName());
      if (d.getArea().getGazetteer() == Gazetteer.ISO && d.getArea().getId() != null && d.getArea().getId().length() >= 2) {
        dist.setCountryCode(d.getArea().getId().substring(0, 2));
      }
      // generate a locality from the area code if missing
      if (dist.getLocality() == null && dist.getLocationID() != null && d.getArea().getGazetteer() != Gazetteer.TEXT) {
        var parsed = SafeParser.parse(AreaParser.PARSER, dist.getLocationID()).orNull();
        if (parsed != null) {
          dist.setLocality(parsed.getName());
        }
      }
    }
    dist.setLifeStage(d.getLifeStage());
    dist.setEstablishmentMeans(str(d.getEstablishmentMeans()));
    dist.setDegreeOfEstablishment(str(d.getDegreeOfEstablishment()));
    dist.setPathway(d.getPathway());
    dist.setThreatStatus(str(d.getThreatStatus()));
    dist.setEventDate(str(d.getYear()));
    dist.setRemarks(d.getRemarks());
    dist.setReferenceID(d.getReferenceId());
    return dist;
  }
  private static String str(Enum<?> e) {
    return e != null ? e.name() : null;
  }
  private static String str(Object e) {
    return e != null ? e.toString() : null;
  }

  Reference convert(life.catalogue.api.model.Reference r) {
    var ref = new Reference();
    ref.setReferenceID(r.getId());
    if (r.getCsl() != null) {
      ref.setDoi(r.getCsl().getDOI());
    }
    ref.setCitation(r.getCitation());
    if (r.getCsl() != null) {
      ref.setUrl(r.getCsl().getURL());
    }
    ref.setRemarks(r.getRemarks());
    return ref;
  }

  MeasurementOrFact convert(TaxonProperty tp) {
    var p = new MeasurementOrFact();
    p.setMeasurementType(tp.getProperty());
    p.setMeasurementValue(tp.getValue());
    p.setMeasurementRemarks(tp.getRemarks());
    return p;
  }

  NameUsageInfo convert(life.catalogue.api.model.UsageInfo ui) {
    var info = new NameUsageInfo();
    var usage = ui.getUsage();

    info.setTaxon(convert(usage, ui.getIssues()));
    info.setGroup(ui.getGroup());
    // acceptedNameUsage
    if (usage.isSynonym()) {
      info.getTaxon().setAcceptedNameUsage(convert(ui.getClassification().getLast()).getLabel());
    }

    // vernacularNames
    if (ui.getVernacularNames() != null) {
      info.setVernacularNames(
        ui.getVernacularNames().stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // classification
    if (ui.getClassification() != null) {
      info.setClassification(
        ui.getClassification().stream()
          .map(this::convert2classification)
          .collect(Collectors.toList())
      );
    }

    // basionym
    if (ui.getNameRelations() != null) {
      ui.getNameRelations().stream().filter(rel -> rel.getType()== NomRelType.BASIONYM).findFirst().ifPresent(rel -> {
        info.getTaxon().setOriginalNameUsageID(rel.getRelatedUsageId());
        if (ui.getSynonyms() != null) {
          ui.getSynonyms().getHomotypic().stream()
              .filter(s -> Objects.equals(s.getId(), rel.getRelatedUsageId()))
              .findFirst().ifPresent(s -> info.getTaxon().setOriginalNameUsage(s.getLabel()));
        }
      });
    }

    // synonyms
    if (ui.getSynonyms() != null) {
      Synonymy syn = new Synonymy();
      syn.setHomotypic(convert(ui.getSynonyms().getHomotypic()));
      syn.setMisapplied(convert(ui.getSynonyms().getMisapplied()));
      syn.setHeterotypic(ui.getSynonyms().getHeterotypicGroups().stream()
        .map(this::convert)
        .collect(Collectors.toList())
      );
      info.setSynonyms(syn);
    }

    // media
    if (ui.getMedia() != null) {
      info.setMedia(
        ui.getMedia().stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // distributions
    if (ui.getDistributions() != null) {
      info.setDistributions(
        ui.getDistributions().stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // measurements
    if (ui.getProperties() != null) {
      info.setMeasurementOrFacts(
        ui.getProperties().stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // publishedIn
    if (ui.getPublishedIn() != null) {
      var pubIn = ui.getPublishedIn();
      info.getTaxon().setNamePublishedInID(pubIn.getId());
      // make sure it's part of the references
      if (ui.getReferences() == null) {
        ui.setReferences(new HashMap<>());
      }
      if (!ui.getReferences().containsKey(pubIn.getId())) {
        ui.getReferences().put(pubIn.getId(), pubIn);
      }
    }
    // references
    if (ui.getReferences() != null) {
      info.setBibliography(
        ui.getReferences().values()
          .stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // checklistBankLink
    info.setChecklistbankURL(
      URI.create(CLB_BASE_URL + usage.getDatasetKey() + "/taxon/" + usage.getId())
    );

    // environment
    if (usage instanceof Taxon tax && tax.getEnvironments() != null) {
      info.setEnvironment(
        tax.getEnvironments().stream()
          .map(Enum::name)
          .collect(Collectors.toList())
      );
    }

    return info;
  }

  private List<NameUsageSimple> convert(List<Synonym> syns) {
    return syns == null ? null : syns.stream().map(this::convert).collect(Collectors.toList());
  }

  public SearchResponse<NameUsageSearchResult, NameUsageSearchParameter> convert(NameUsageSearchResponse resp) {
    List<NameUsageSearchResult> results = resp.getResult() == null ? List.of() :
      resp.getResult().stream().map(this::convert).collect(Collectors.toList());
    List<Facet<NameUsageSearchParameter>> facets = null;
    if (resp.getFacets() != null && !resp.getFacets().isEmpty()) {
      facets = new ArrayList<>();
      for (var entry : resp.getFacets().entrySet()) {
        var gbifParam = CLB_TO_GBIF_PARAM.get(entry.getKey());
        if (gbifParam == null) continue; // CLB parameter has no GBIF equivalent
        var counts = entry.getValue().stream()
          .map(fv -> new Facet.Count(
            fv.getLabel() != null ? fv.getLabel() : String.valueOf(fv.getValue()),
            (long) fv.getCount()
          ))
          .collect(Collectors.toList());
        facets.add(new Facet<>(gbifParam, counts));
      }
    }
    return new SearchResponse<>(resp.getOffset(), resp.getLimit(), (long) resp.getTotal(), results, facets);
  }

  private NameUsageSearchResult convert(NameUsageWrapper wrapper) {
    var result = new NameUsageSearchResult();
    if (wrapper.getUsage() != null) {
      result.setTaxon(convert(wrapper.getUsage().asUsageBase()));
    }
    result.setGroup(wrapper.getGroup());
    if (wrapper.getClassification() != null) {
      result.setClassification(
        wrapper.getClassification().stream()
            .filter(u -> u.getStatus() == null || !u.getStatus().isSynonym())
            .filter(u -> !Objects.equals(wrapper.getId(), u.getId()))
            .map(this::convert2classification)
            .collect(Collectors.toList())
      );
    }
    if (wrapper.getVernacularNames() != null) {
      result.setVernacularNames(
        wrapper.getVernacularNames().stream().map(svn -> {
          var vn = new VernacularNameSimple();
          vn.setVernacularName(svn.getName());
          vn.setLanguage(svn.getLanguage());
          return vn;
        }).collect(Collectors.toList())
      );
    }
    return result;
  }

  public NameUsageSuggestResult convert(NameUsageSuggestion suggest) {
    var result = new NameUsageSuggestResult();
    result.setTaxonID(suggest.getUsageId());
    result.setScientificNameID(suggest.getNameId());
    result.setScientificName(suggest.getMatch());
    result.setTaxonRank(suggest.getRank());
    result.setTaxonomicStatus(suggest.getStatus());
    result.setNomenclaturalCode(str(suggest.getNomCode()));
    result.setAcceptedNameUsageID(suggest.getAcceptedUsageId());
    result.setAcceptedNameUsage(suggest.getAcceptedName());
    result.setGroup(suggest.getGroup());
    result.setContext(suggest.getContext());
    //result.setScore(suggest.getScore());
    return result;
  }


  public life.catalogue.api.search.NameUsageSearchRequest convert(NameUsageSearchRequest request) {
    var req = new life.catalogue.api.search.NameUsageSearchRequest();
    copyCommon(request, req);
    if (request.getSortBy() != null) {
      req.setSortBy(request.getSortBy());
    }
    if (request.getQFields() != null && !request.getQFields().isEmpty()) {
      req.setContent(request.getQFields().stream()
        .filter(f -> f instanceof NameUsageSearchRequest.NameUsageQueryField)
        .map(f -> ((NameUsageSearchRequest.NameUsageQueryField) f).clbValue)
        .collect(Collectors.toSet()));
    }
    if (request.getFacets() != null && !request.getFacets().isEmpty()) {
      req.setFacets(request.getFacets().stream()
        .map(NameUsageSearchParameter::toClb)
        .collect(Collectors.toSet()));
    }
    req.setFacetMinCount(request.getFacetMinCount());
    req.setFacetLimit(request.getFacetLimit());
    req.setFacetOffset(request.getFacetOffset());
    return req;
  }

  public life.catalogue.api.search.NameUsageSuggestRequest convert(NameUsageSuggestRequest request) {
    var req = new life.catalogue.api.search.NameUsageSuggestRequest();
    copyCommon(request, req);
    return req;
  }


  /**
   * Translates the shared request parameters for search and suggest from the GBIF v2 taxon API
   * to the CLB search API.
   * @param from the GBIF API request
   * @param req the CLB API request to translate to
   */
  private void copyCommon(SearchRequest<NameUsageSearchParameter> from, life.catalogue.api.search.NameUsageRequest req) {
    req.setQ(from.getQ());
    req.setSortBy(NameUsageRequest.SortBy.RELEVANCE);
    if (from.getParameters() != null) {
      Map<life.catalogue.api.search.NameUsageSearchParameter, Set<Object>> filters = new EnumMap<>(life.catalogue.api.search.NameUsageSearchParameter.class);
      for (var entry : from.getParameters().entrySet()) {
        var values = entry.getValue();
        if (values == null || values.isEmpty()) continue;
        var clbParam = entry.getKey().toClb();
        if (entry.getKey() == NameUsageSearchParameter.DATASET_KEY) {
          Set<Object> keys = new HashSet<>();
          for (String key : values) {
            keys.add(map.toCLB(UUID.fromString(key)));
          }
          filters.put(clbParam, keys);
        } else {
          filters.put(clbParam, new HashSet<>(values));
        }
      }
      req.setFilters(filters);
    }
  }

  public ChecklistMetrics convert(DatasetImport imp) {
    var metrics = new ChecklistMetrics();
    metrics.setOrigin(imp.getOrigin());
    metrics.setFormat(imp.getFormat());
    metrics.setDownload(imp.getDownload());
    metrics.setAttempt(imp.getAttempt());
    metrics.setState(imp.getState());
    metrics.setStarted(imp.getStarted());
    metrics.setFinished(imp.getFinished());

    metrics.setBareNameCount(imp.getBareNameCount());
    metrics.setDistributionCount(imp.getDistributionCount());
    metrics.setEstimateCount(imp.getEstimateCount());
    metrics.setMediaCount(imp.getMediaCount());
    metrics.setNameCount(imp.getNameCount());
    metrics.setReferenceCount(imp.getReferenceCount());
    metrics.setSynonymCount(imp.getSynonymCount());
    metrics.setTaxonCount(imp.getTaxonCount());
    metrics.setTreatmentCount(imp.getTreatmentCount());
    metrics.setTypeMaterialCount(imp.getTypeMaterialCount());
    metrics.setVernacularCount(imp.getVernacularCount());

    metrics.setNamesByCodeCount(imp.getNamesByCodeCount());
    metrics.setNamesByRankCount(imp.getNamesByRankCount());
    metrics.setNamesByTypeCount(imp.getNamesByTypeCount());
    metrics.setTaxaByRankCount(imp.getTaxaByRankCount());
    metrics.setUsagesByStatusCount(imp.getUsagesByStatusCount());
    metrics.setUsagesByOriginCount(imp.getUsagesByOriginCount());

    return metrics;
  }
}
