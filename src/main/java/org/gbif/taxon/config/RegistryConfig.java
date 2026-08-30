package org.gbif.taxon.config;

import org.gbif.taxon.registry.RegistryDatasetClient;
import org.gbif.ws.client.ClientBuilder;
import org.gbif.ws.json.JacksonJsonObjectMapperProvider;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Optional write access to the GBIF registry, used to keep the COL dataset pointing at the
 * ChecklistBank release that is actually in use.
 * The client only exists when registry.enabled is true and an app key is configured, so local and
 * dev instances never write to the registry and a deployment missing its credentials degrades to
 * doing nothing rather than failing an authenticated call every hour.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "registry")
public class RegistryConfig {

  private boolean enabled = false;
  private String api = "https://api.gbif.org/v1/";
  private String username;
  private String appKey;
  private String secret;

  /**
   * @return true if registry writing is switched on but cannot work for lack of credentials
   */
  public boolean isMisconfigured() {
    return enabled && (appKey == null || appKey.isBlank());
  }

  @Bean
  @ConditionalOnExpression("${registry.enabled:false} && !'${registry.appKey:}'.isEmpty()")
  public RegistryDatasetClient registryDatasetClient() {
    return new ClientBuilder()
      // the object mapper must be set before the credentials, which capture it for content signing
      .withObjectMapper(JacksonJsonObjectMapperProvider.getObjectMapperWithBuilderSupport())
      .withAppKeyCredentials(username, appKey, secret)
      .withUrl(api)
      .build(RegistryDatasetClient.class);
  }
}
