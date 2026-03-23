package org.gbif.taxon.api.search;

import life.catalogue.api.vocab.TaxGroup;


import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import life.catalogue.api.vocab.Environment;
import life.catalogue.api.vocab.Issue;
import life.catalogue.api.vocab.Origin;
import life.catalogue.api.vocab.TaxonomicStatus;


@JsonDeserialize(
  as = NameUsageSearchParameter.class
)
public enum NameUsageSearchParameter implements SearchParameter {
  /**
   * A taxonID that searches on the entire classification of a Taxon or its Synonyms. E.g. searching by the taxonID for Coleoptera should
   * return all name usages within that beetle order, including synonyms.
   */
  TAXON_ID(String.class, life.catalogue.api.search.NameUsageSearchParameter.TAXON_ID),
  DATASET_KEY(UUID.class, life.catalogue.api.search.NameUsageSearchParameter.DATASET_KEY),
  RANK(Rank.class, life.catalogue.api.search.NameUsageSearchParameter.RANK),
  STATUS(TaxonomicStatus.class, life.catalogue.api.search.NameUsageSearchParameter.STATUS),
  EXTINCT(Boolean.class, life.catalogue.api.search.NameUsageSearchParameter.EXTINCT),
  ENVIRONMENT(Environment.class, life.catalogue.api.search.NameUsageSearchParameter.ENVIRONMENT),
  GROUP(TaxGroup.class, life.catalogue.api.search.NameUsageSearchParameter.GROUP),
  CODE(NomCode.class, life.catalogue.api.search.NameUsageSearchParameter.NOM_CODE),
  NAME_TYPE(NameType.class, life.catalogue.api.search.NameUsageSearchParameter.NAME_TYPE),
  AUTHOR(String.class, life.catalogue.api.search.NameUsageSearchParameter.AUTHORSHIP),
  YEAR(String.class, life.catalogue.api.search.NameUsageSearchParameter.AUTHORSHIP_YEAR),
  ISSUE(Issue.class, life.catalogue.api.search.NameUsageSearchParameter.ISSUE),
  ORIGIN(Origin.class, life.catalogue.api.search.NameUsageSearchParameter.ORIGIN);

  private final Class<?> type;
  private final life.catalogue.api.search.NameUsageSearchParameter clbParam;

  private NameUsageSearchParameter(Class<?> type, life.catalogue.api.search.NameUsageSearchParameter clbParam) {
    this.type = type;
    this.clbParam = clbParam;
  }

  public Class<?> type() {
    return this.type;
  }

  public life.catalogue.api.search.NameUsageSearchParameter toClb() {
    return clbParam;
  }
}
