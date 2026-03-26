/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.taxon.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gbif.taxon.provider.NameUsageSearchRequestHandlerMethodArgumentResolver;
import org.gbif.taxon.provider.NameUsageSuggestRequestHandlerMethodArgumentResolver;
import org.gbif.ws.json.JacksonJsonObjectMapperProvider;
import org.gbif.ws.server.provider.CountryHandlerMethodArgumentResolver;
import org.gbif.ws.server.provider.PageableHandlerMethodArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Bean
  public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowSemicolon(true);
    return firewall;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
    argumentResolvers.add(new CountryHandlerMethodArgumentResolver());
    argumentResolvers.add(new NameUsageSearchRequestHandlerMethodArgumentResolver());
    argumentResolvers.add(new NameUsageSuggestRequestHandlerMethodArgumentResolver());
  }

  @Primary
  @Bean
  public ObjectMapper registryObjectMapper() {
    var om = JacksonJsonObjectMapperProvider.getObjectMapper();
    // support Java 8 date time apis
    om.registerModule(new JavaTimeModule());
    return om;
  }
}
