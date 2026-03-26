package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.Hidden;
import life.catalogue.api.search.NameUsageRequest;
import lombok.Getter;
import lombok.Setter;
import org.gbif.api.model.common.search.FacetedSearchRequest;

public class NameUsageSearchRequest extends FacetedSearchRequest<NameUsageSearchParameter> {
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

  @Override
  public void setHighlight(boolean highlight) {
    throw new UnsupportedOperationException("Highlight parameter is not supported in taxon operations.");
  }

  @Override
  public void setSpellCheck(boolean spellCheck) {
    throw new UnsupportedOperationException("Spell check parameter is supported in taxon operations.");
  }

  @Override
  public void setSpellCheckCount(int spellCheckCount) {
    // the request provider sets this to -1 by default, so we need to allow for that
    if (spellCheckCount != -1) {
      throw new UnsupportedOperationException("Spell check count parameter is supported in taxon operations.");
    }
  }

  @Override
  public void setMatchCase(Boolean matchCase) {
    throw new UnsupportedOperationException("Match case parameter is supported in taxon operations.");
  }

  @Override
  public void setShuffle(String shuffle) {
    throw new UnsupportedOperationException("Shuffle parameter is supported in taxon operations.");
  }
}
