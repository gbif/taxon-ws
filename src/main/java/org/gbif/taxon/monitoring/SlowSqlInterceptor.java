package org.gbif.taxon.monitoring;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MyBatis interceptor that logs SQL statements whose execution exceeds a configurable threshold.
 * Captures the mapper statement id and bound parameter object — enough to correlate with the
 * SQL definition without dumping the full bound statement.
 */
@Intercepts({
  @Signature(type = Executor.class, method = "query",
      args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SlowSqlInterceptor implements Interceptor {
  private static final Logger LOG = LoggerFactory.getLogger(SlowSqlInterceptor.class);

  private final long thresholdMs;

  public SlowSqlInterceptor(long thresholdMs) {
    this.thresholdMs = thresholdMs;
  }

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    long start = System.nanoTime();
    try {
      return invocation.proceed();
    } finally {
      long durationMs = (System.nanoTime() - start) / 1_000_000L;
      if (durationMs >= thresholdMs) {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args.length > 1 ? args[1] : null;
        LOG.warn("SLOW SQL {}ms {} params={}", durationMs, ms.getId(), parameter);
      }
    }
  }

  @Override
  public void setProperties(Properties properties) {
    // no-op
  }
}
