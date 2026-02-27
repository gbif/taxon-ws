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
  DATASET_KEY(UUID.class),
  RANK(Rank.class),
  /**
   * A taxonID that searches on the entire classification of a Taxon or its Synonyms. E.g. searching by the taxonID for Coleoptera should
   * return all name usages within that beetle order, including synonyms.
   */
  TAXON_ID(String.class),
  STATUS(TaxonomicStatus.class),
  EXTINCT(Boolean.class),
  ENVIRONMENT(Environment.class),
  GROUP(TaxGroup.class),
  NOM_CODE(NomCode.class),
  NAME_TYPE(NameType.class),
  ISSUE(Issue.class),
  ORIGIN(Origin.class);

  private final Class<?> type;

  private NameUsageSearchParameter(Class<?> type) {
    this.type = type;
  }

  public Class<?> type() {
    return this.type;
  }
}
