package org.gbif.taxon.dao;

import life.catalogue.api.model.DatasetImport;
import life.catalogue.api.model.NameUsageBase;
import life.catalogue.api.model.SimpleName;
import life.catalogue.api.model.SimpleNameInDataset;
import life.catalogue.api.model.Synonym;
import life.catalogue.api.model.Taxon;
import life.catalogue.api.model.TaxonProperty;
import life.catalogue.api.model.TreeNode;
import life.catalogue.api.search.NameUsageRequest;
import life.catalogue.api.search.NameUsageSearchResponse;
import life.catalogue.api.search.NameUsageSuggestion;
import life.catalogue.api.search.NameUsageWrapper;
import life.catalogue.api.vocab.Country;


import org.gbif.api.model.common.search.Facet;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.taxon.api.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchResult;


import org.gbif.taxon.api.search.NameUsageSuggestRequest;
import org.gbif.taxon.api.search.NameUsageSuggestResult;


import org.springframework.stereotype.Component;

@Component
public class ApiConverter {
  private static final String CLB_BASE_URL = "https://www.checklistbank.org/dataset/";

  /** Reverse map from CLB search parameter to the corresponding GBIF v2 parameter. */
  private static final Map<life.catalogue.api.search.NameUsageSearchParameter, NameUsageSearchParameter> CLB_TO_GBIF_PARAM;
  static {
    CLB_TO_GBIF_PARAM = new EnumMap<>(life.catalogue.api.search.NameUsageSearchParameter.class);
    for (var p : NameUsageSearchParameter.values()) {
      CLB_TO_GBIF_PARAM.put(p.toClb(), p);
    }
  }

  private DatasetKeyMap map;

  public ApiConverter(DatasetKeyMap map) {
    this.map = map;
  }

  NameUsage convert(NameUsageBase nub) {
    var nu = new NameUsage();
    var name = nub.getName();

    // SimpleUsage fields
    nu.setTaxonID(nub.getId());
    if (nub.getStatus().isSynonym()) {
      nu.setAcceptedNameUsageID(nub.getParentId());
    } else {
      nu.setParentNameUsageID(nub.getParentId());
    }
    nu.setScientificName(name.getScientificName());
    nu.setScientificNameAuthorship(name.getAuthorship());
    nu.setTaxonRank(name.getRank());
    nu.setTaxonomicStatus(nub.getStatus());
    nu.setNomenclaturalCode(name.getCode() != null ? name.getCode().name() : null);
    if (nub instanceof Taxon tax) {
      nu.setExtinct(tax.isExtinct());
    }
    nu.setLabel(nub.getLabelHtml());

    // NameUsage-specific fields
    nu.setDatasetKey(map.toGBIF(nub.getDatasetKey()));
    nu.setScientificNameID(name.getId());
    if (nub instanceof Synonym syn && syn.getAccepted() != null) {
      nu.setAcceptedNameUsage(syn.getAccepted().getLabel());
    }
    nu.setNameAccordingTo(nub.getAccordingTo());
    nu.setNamePhrase(nub.getNamePhrase());
    nu.setNomenclaturalStatus(name.getNomStatus() != null ? name.getNomStatus().name() : null);
    nu.setNameType(name.getType());
    nu.setGenericName(name.getGenus());
    nu.setInfragenericEpithet(name.getInfragenericEpithet());
    nu.setSpecificEpithet(name.getSpecificEpithet());
    nu.setInfraspecificEpithet(name.getInfraspecificEpithet());
    nu.setCultivarEpithet(name.getCultivarEpithet());
    nu.setReferences(nub.getLink());
    nu.setTaxonRemarks(nub.getRemarks());

    return nu;
  }

  public NameUsageSimple convert(SimpleNameInDataset sn) {
    var su = convert((SimpleName)sn);
    su.setDatasetKey(map.toGBIF(sn.getDatasetKey()));
    return su;
  }

  public NameUsageSimple convert(SimpleName sn) {
    var su = new NameUsageSimple();

    su.setTaxonID(sn.getId());
    if (sn.getStatus() != null && sn.getStatus().isSynonym()) {
      su.setAcceptedNameUsageID(sn.getParentId());
    } else {
      su.setParentNameUsageID(sn.getParentId());
    }
    su.setScientificName(sn.getName());
    su.setScientificNameAuthorship(sn.getAuthorship());
    su.setTaxonRank(sn.getRank());
    su.setTaxonomicStatus(sn.getStatus());
    su.setNomenclaturalCode(sn.getCode() != null ? sn.getCode().name() : null);
    su.setExtinct(sn.isExtinct());
    su.setLabel(sn.getLabelHtml());

    return su;
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
    media.setType(m.getType() != null ? m.getType().name() : null);
    media.setTitle(m.getTitle());
    media.setCreated(m.getCaptured() != null ? m.getCaptured().toString() : null);
    media.setCreator(m.getCapturedBy());
    media.setLicense(m.getLicense() != null ? m.getLicense().name() : null);
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
    v.setSex(vn.getSex() != null ? vn.getSex().name() : null);
    v.setPreferredName(vn.isPreferred());
    v.setRemarks(vn.getRemarks());
    return v;
  }

  Distribution convert(life.catalogue.api.model.Distribution d) {
    var dist = new Distribution();
    if (d.getArea() != null) {
      dist.setLocality(d.getArea().getName());
      if (d.getArea() instanceof Country c) {
        dist.setCountryCode(c.getIso2LetterCode());
      }
    }
    dist.setLifeStage(d.getLifeStage());
    dist.setEstablishmentMeans(d.getEstablishmentMeans() != null ? d.getEstablishmentMeans().name() : null);
    dist.setDegreeOfEstablishment(d.getDegreeOfEstablishment() != null ? d.getDegreeOfEstablishment().name() : null);
    dist.setPathway(d.getPathway());
    dist.setThreatStatus(d.getThreatStatus() != null ? d.getThreatStatus().name() : null);
    dist.setEventDate(d.getYear() != null ? String.valueOf(d.getYear()) : null);
    dist.setRemarks(d.getRemarks());
    //TODO: expand refID with citation
    dist.setSource(d.getReferenceId());
    return dist;
  }

  Reference convert(life.catalogue.api.model.Reference r) {
    var ref = new Reference();
    ref.setReferenceID(r.getId());
    if (r.getCsl() != null) {
      ref.setDoi(r.getCsl().getDOI());
    }
    ref.setCitation(r.getCitation());
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

  UsageInfo convert(life.catalogue.api.model.UsageInfo ui) {
    var info = new UsageInfo();
    var usage = ui.getUsage();

    info.setTaxon(convert(usage));
    info.setGroup(ui.getGroup());

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
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // synonyms
    if (ui.getSynonyms() != null) {
      info.setSynonyms(
        ui.getSynonyms().all().stream()
          .map(SimpleName::new)
          .map(this::convert)
          .collect(Collectors.toList())
      );
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

    // bibliography
    if (ui.getReferences() != null) {
      info.setBibliography(
        ui.getReferences().values()
          .stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // checklistBankLink
    info.setChecklistBankLink(
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
      result.setUsage(convert(wrapper.getUsage().asUsageBase()));
    }
    result.setGroup(wrapper.getGroup());
    if (wrapper.getClassification() != null) {
      result.setClassification(
        wrapper.getClassification().stream().map(this::convert).collect(Collectors.toList())
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
    result.setNomenclaturalCode(suggest.getNomCode() != null ? suggest.getNomCode().name() : null);
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
    if (request.getQFields() != null && !request.getQFields().isEmpty()) {
      req.setContent(request.getQFields().stream()
        .filter(f -> f instanceof NameUsageSearchRequest.NameUsageQueryField)
        .map(f -> ((NameUsageSearchRequest.NameUsageQueryField) f).clbValue)
        .collect(Collectors.toSet()));
    }
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
