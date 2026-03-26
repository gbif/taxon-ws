package org.gbif.taxon.provider;

import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSuggestRequest;
import org.gbif.ws.server.provider.SearchRequestProvider;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link NameUsageSuggestRequest} from HTTP query parameters.
 * Also validates that no unknown query parameters are present.
 */
public class NameUsageSuggestRequestHandlerMethodArgumentResolver
    extends SearchRequestProvider<NameUsageSuggestRequest, NameUsageSearchParameter>
    implements HandlerMethodArgumentResolver {

  public NameUsageSuggestRequestHandlerMethodArgumentResolver() {
    super(NameUsageSuggestRequest.class, NameUsageSearchParameter.class);
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return NameUsageSuggestRequest.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    NameUsageRequestHandlerMethodArgumentResolver.validateQueryParams(webRequest.getParameterMap());
    return getValue(webRequest);
  }
}
