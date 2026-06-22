package org.gbif.taxon.provider;

import life.catalogue.api.search.NameUsageRequest;
import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchRequest.SearchContent;
import org.gbif.ws.server.provider.FacetedSearchRequestProvider;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves {@link NameUsageSearchRequest} from HTTP query parameters.
 * Also validates that no unknown query parameters are present.
 */
public class NameUsageSearchRequestHandlerMethodArgumentResolver
    extends FacetedSearchRequestProvider<NameUsageSearchRequest, NameUsageSearchParameter>
    implements HandlerMethodArgumentResolver {

  public NameUsageSearchRequestHandlerMethodArgumentResolver() {
    super(NameUsageSearchRequest.class, NameUsageSearchParameter.class);
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return NameUsageSearchRequest.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    NameUsageRequestHandlerMethodArgumentResolver.validateQueryParams(webRequest.getParameterMap());
    return getValue(webRequest);
  }

  /**
   * Overrides the parent to re-parse {@code qField} using {@link SearchContent}.
   * The base {@link FacetedSearchRequestProvider} hardcodes the old GBIF v1
   * {@code NameUsageQueryField} class and silently drops unrecognized values.
   */
  @Override
  protected NameUsageSearchRequest getSearchRequest(WebRequest webRequest, NameUsageSearchRequest request) {
    final var req = super.getSearchRequest(webRequest, request);
    final Map<String, String[]> params = webRequest.getParameterMap();
    String sortBy = getFirstIgnoringCase("sortBy", params);
    if (sortBy != null) {
      try {
        req.setSortBy(NameUsageRequest.SortBy.valueOf(sortBy.toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unknown sortBy value: " + sortBy +
            ". Valid values are: " + Arrays.toString(NameUsageRequest.SortBy.values()));
      }
    }
    String reverse = getFirstIgnoringCase("reverse", params);
    if (reverse != null) {
      req.setReverse("true".equalsIgnoreCase(reverse.trim()));
    }
    String searchType = getFirstIgnoringCase("searchType", params);
    if (searchType != null) {
      try {
        req.setSearchType(NameUsageSearchRequest.SearchType.valueOf(searchType.toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unknown searchType value: " + searchType +
            ". Valid values are: " + Arrays.toString(NameUsageSearchRequest.SearchType.values()));
      }
    }

    String[] searchContent = params.get("searchContent");
    if (searchContent != null && searchContent.length > 0) {
      try {
        req.setSearchContent(
            Arrays.stream(searchContent)
                .map(sc -> NameUsageSearchRequest.SearchContent.valueOf(sc.toUpperCase()))
                .collect(Collectors.toSet())
        );
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unknown searchContent value: " + Arrays.toString(searchContent) +
            ". Valid values are: " + Arrays.toString(NameUsageSearchRequest.SearchContent.values()));
      }
    }
    return req;
  }
}
