package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.Hidden;
import life.catalogue.api.search.NameUsageRequest;
import lombok.Getter;
import lombok.Setter;
import org.gbif.api.model.common.search.FacetedSearchRequest;

import java.util.Set;

public class NameUsageSearchRequest extends FacetedSearchRequest<NameUsageSearchParameter> {

  @Hidden @Getter @Setter
  private SearchType searchType;

  @Hidden @Getter @Setter
  private Set<SearchContent> searchContent;

  @Hidden @Getter @Setter
  private NameUsageRequest.SortBy sortBy;

  @Hidden @Getter @Setter
  private boolean reverse;

  public enum SearchContent {
    SCIENTIFIC(NameUsageRequest.SearchContent.SCIENTIFIC_NAME),
    AUTHORSHIP(NameUsageRequest.SearchContent.AUTHORSHIP),
    VERNACULAR(NameUsageRequest.SearchContent.VERNACULAR_NAME);

    public final NameUsageRequest.SearchContent clbValue;

    SearchContent(NameUsageRequest.SearchContent clbValue) {
      this.clbValue = clbValue;
    }
  }

  public enum SearchType {
    WORDS(NameUsageRequest.SearchType.STANDARD),
    EXACT(NameUsageRequest.SearchType.EXACT),
    FUZZY(NameUsageRequest.SearchType.FUZZY);

    public final NameUsageRequest.SearchType clbValue;

    SearchType(NameUsageRequest.SearchType clbValue) {
      this.clbValue = clbValue;
    }
  }

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
