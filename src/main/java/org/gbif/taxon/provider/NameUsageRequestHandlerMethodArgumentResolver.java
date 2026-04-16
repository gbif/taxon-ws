package org.gbif.taxon.provider;

import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.ws.server.provider.SearchRequestProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared validation logic for name usage search and suggest request resolvers. */
class NameUsageRequestHandlerMethodArgumentResolver {

  /** Standard paging and search params accepted in addition to {@link NameUsageSearchParameter} values. */
  static final Set<String> STANDARD_PARAMS = Set.of(
      "Q", "OFFSET", "LIMIT", "QFIELD",
      "SORTBY", "REVERSE",
      "FACET", "FACETMINCOUNT", "FACETLIMIT", "FACETOFFSET", "FACETMULTISELECT"
  );

  /** Normalized names of all known {@link NameUsageSearchParameter} values. */
  static final Set<String> KNOWN_SEARCH_PARAMS = Arrays.stream(NameUsageSearchParameter.values())
      .map(p -> normalize(p.name()))
      .collect(Collectors.toUnmodifiableSet());

  private NameUsageRequestHandlerMethodArgumentResolver() {}

  /**
   * Throws {@link IllegalArgumentException} if any query parameter name is not recognized.
   */
  static void validateQueryParams(Map<String, String[]> params) {
    for (String name : params.keySet()) {
      String normalized = normalize(name);
      if (!STANDARD_PARAMS.contains(normalized) && !KNOWN_SEARCH_PARAMS.contains(normalized)) {
        throw new IllegalArgumentException("Unknown query parameter: " + name);
      }
    }
  }

  /** Matches the normalization used by {@link SearchRequestProvider#findSearchParam(String)}. */
  static String normalize(String name) {
    return name.toUpperCase().replaceAll("[. _-]", "");
  }
}
