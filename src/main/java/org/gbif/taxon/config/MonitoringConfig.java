package org.gbif.taxon.config;

import lombok.Data;
import org.gbif.taxon.monitoring.SlowRequestFilter;
import org.gbif.taxon.monitoring.SlowSqlInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Toggleable performance monitoring: a slow HTTP request filter and a slow MyBatis SQL interceptor.
 * Configured via the {@code monitoring.*} properties — both off by default to avoid log noise.
 */
@Configuration
@ConfigurationProperties(prefix = "monitoring")
@Data
public class MonitoringConfig {

  private Threshold slowRequest = new Threshold();
  private Threshold slowSql = new Threshold();

  @Data
  public static class Threshold {
    private boolean enabled = false;
    private long thresholdMs = 1000L;
  }

  @Bean
  @ConditionalOnProperty(prefix = "monitoring.slow-request", name = "enabled", havingValue = "true")
  public SlowRequestFilter slowRequestFilter() {
    return new SlowRequestFilter(slowRequest.getThresholdMs());
  }

  @Bean
  @ConditionalOnProperty(prefix = "monitoring.slow-sql", name = "enabled", havingValue = "true")
  public SlowSqlInterceptor slowSqlInterceptor() {
    return new SlowSqlInterceptor(slowSql.getThresholdMs());
  }
}
