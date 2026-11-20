package org.gbif.taxon.provider;

import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Facet paging bounds inherited from gbif-common-ws 2.1.4's FacetedSearchRequestProvider.
 */
class NameUsageSearchRequestHandlerMethodArgumentResolverTest {

  private final NameUsageSearchRequestHandlerMethodArgumentResolver resolver = new NameUsageSearchRequestHandlerMethodArgumentResolver();

  private NameUsageSearchRequest resolve(String... params) {
    var req = new MockHttpServletRequest("GET", "/species/search");
    for (int i = 0; i < params.length; i += 2) {
      req.addParameter(params[i], params[i + 1]);
    }
    return (NameUsageSearchRequest) resolver.resolveArgument(null, null, new ServletWebRequest(req), null);
  }

  @Test
  void facetLimitIsCappedAt1000() {
    var req = resolve("facet", "taxonRank", "facetLimit", "2000");
    assertThat(req.getFacets()).containsExactly(NameUsageSearchParameter.TAXON_RANK);
    assertThat(req.getFacetLimit()).isEqualTo(1000);
  }

  @Test
  void facetOffsetAbove10000IsRejected() {
    assertThat(resolve("facet", "taxonRank", "facetOffset", "10000").getFacetOffset()).isEqualTo(10000);
    assertThatThrownBy(() -> resolve("facet", "taxonRank", "facetOffset", "10001"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void invalidFacetNumbersAreRejected() {
    assertThatThrownBy(() -> resolve("facet", "taxonRank", "facetLimit", "-1"))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> resolve("facet", "taxonRank", "facetMinCount", "x"))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
