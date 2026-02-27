package org.gbif.taxon.api.search;

import java.util.Set;

import io.swagger.v3.oas.annotations.Hidden;
import life.catalogue.api.search.NameUsageRequest;
import lombok.Getter;
import lombok.Setter;

public class NameUsageSearchRequest extends BaseNameUsageRequest {
  public enum NameUsageQueryField implements QueryField {
    SCIENTIFIC(NameUsageRequest.SearchContent.SCIENTIFIC_NAME),
    AUTHORSHIP(NameUsageRequest.SearchContent.AUTHORSHIP),
    VERNACULAR(NameUsageRequest.SearchContent.VERNACULAR_NAME);

    public final NameUsageRequest.SearchContent clbValue;

    NameUsageQueryField(NameUsageRequest.SearchContent clbValue) {
      this.clbValue = clbValue;
    }
  }

  @Hidden @Getter @Setter
  private NameUsageRequest.SearchType searchType;

}
