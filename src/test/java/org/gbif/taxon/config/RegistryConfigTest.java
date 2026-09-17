package org.gbif.taxon.config;

import org.gbif.taxon.registry.RegistryDatasetClient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryConfigTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class))
    .withUserConfiguration(RegistryConfig.class);

  /**
   * Builds the client for real. The feign contract parses and validates every method signature at
   * target time, so this fails if any of the annotations on RegistryDatasetClient are wrong.
   */
  @Test
  void clientBuiltWhenEnabledAndConfigured() {
    runner
      .withPropertyValues("registry.enabled=true", "registry.appKey=key", "registry.secret=sec", "registry.username=taxon-ws")
      .run(ctx -> assertThat(ctx).hasSingleBean(RegistryDatasetClient.class));
  }

  @Test
  void noClientWhenDisabled() {
    runner
      .withPropertyValues("registry.enabled=false", "registry.appKey=key")
      .run(ctx -> assertThat(ctx).doesNotHaveBean(RegistryDatasetClient.class));
  }

  @Test
  void noClientWhenEnabledButCredentialsMissing() {
    runner
      .withPropertyValues("registry.enabled=true")
      .run(ctx -> {
        assertThat(ctx).doesNotHaveBean(RegistryDatasetClient.class);
        assertThat(ctx.getBean(RegistryConfig.class).isMisconfigured()).isTrue();
      });
  }
}
