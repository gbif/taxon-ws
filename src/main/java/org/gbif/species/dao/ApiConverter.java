package org.gbif.species.dao;

import life.catalogue.api.model.NameUsageBase;
import life.catalogue.api.model.SimpleName;
import life.catalogue.api.model.Synonym;
import life.catalogue.api.model.Taxon;
import life.catalogue.api.model.TaxonProperty;
import life.catalogue.api.model.TreeNode;
import life.catalogue.api.vocab.Country;

import org.gbif.species.api.*;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ApiConverter {
  private static final String CLB_BASE_URL = "https://www.checklistbank.org/dataset/";

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
    nu.setNameAccordingToID(nub.getAccordingToId());
    nu.setNameAccordingTo(nub.getAccordingTo());
    nu.setNamePublishedInID(name.getPublishedInId());
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

  SimpleUsage convertSimple(SimpleName sn) {
    var su = new SimpleUsage();

    su.setTaxonID(sn.getId());
    if (sn.getStatus().isSynonym()) {
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

  UsageInfo convert(life.catalogue.api.model.UsageInfo ui) {
    var info = new UsageInfo();
    var usage = ui.getUsage();

    info.setTaxonID(usage.getId());

    // namePublishedIn
    if (ui.getPublishedIn() != null) {
      info.setNamePublishedIn(convert(ui.getPublishedIn()));
    }

    // nameAccordingTo — look up accordingToId in the references map
    if (usage.getAccordingToId() != null && ui.getReferences() != null) {
      var accordingToRef = ui.getReferences().get(usage.getAccordingToId());
      if (accordingToRef != null) {
        info.setNameAccordingTo(convert(accordingToRef));
      }
    }

    // vernacularNames
    if (ui.getVernacularNames() != null) {
      info.setVernacularNames(
        ui.getVernacularNames().stream()
          .map(this::convert)
          .collect(Collectors.toList())
      );
    }

    // synonyms
    if (ui.getSynonyms() != null) {
      info.setSynonyms(
        ui.getSynonyms().all().stream()
          .map(SimpleName::new)
          .map(this::convertSimple)
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

    // bibliography
    if (ui.getReferences() != null) {
      info.setBibliography(
        ui.getReferences().values().stream()
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

    // properties: iucnRedlistStatus, citesAppendix, citesDateAdded
    if (ui.getProperties() != null) {
      for (TaxonProperty prop : ui.getProperties()) {
        if (prop.getProperty() != null && prop.getValue() != null) {
          switch (prop.getProperty()) {
            case "iucnRedlistStatus" -> info.setIucnRedlistStatus(prop.getValue());
            case "citesAppendix" -> info.setCitesAppendix(prop.getValue());
            case "citesDateAdded" -> info.setCitesDateAdded(prop.getValue());
          }
        }
      }
    }

    return info;
  }
}
