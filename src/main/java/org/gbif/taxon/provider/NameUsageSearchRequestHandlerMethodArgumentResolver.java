package org.gbif.taxon.provider;

import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchRequest.NameUsageQueryField;
import org.gbif.ws.server.provider.FacetedSearchRequestProvider;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Set;
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
   * Overrides the parent to re-parse {@code qField} using {@link NameUsageQueryField}.
   * The base {@link FacetedSearchRequestProvider} hardcodes the old GBIF v1
   * {@code NameUsageQueryField} class and silently drops unrecognized values.
   */
  @Override
  protected NameUsageSearchRequest getSearchRequest(WebRequest webRequest, NameUsageSearchRequest request) {
    var req = super.getSearchRequest(webRequest, request);
    String[] qFieldValues = webRequest.getParameterValues("qField");
    if (qFieldValues != null) {
      Set<SearchRequest.QueryField> qFields = Arrays.stream(qFieldValues)
          .map(v -> {
            try {
              return NameUsageQueryField.valueOf(v.toUpperCase());
            } catch (IllegalArgumentException e) {
              throw new IllegalArgumentException("Unknown qField value: " + v +
                  ". Valid values are: " + Arrays.toString(NameUsageQueryField.values()));
            }
          })
          .collect(Collectors.toSet());
      req.setQFields(qFields);
    }
    return req;
  }
}
