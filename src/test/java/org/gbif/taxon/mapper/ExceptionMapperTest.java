package org.gbif.taxon.mapper;

import life.catalogue.api.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionMapperTest {

  private final ExceptionMapper mapper = new ExceptionMapper();
  private ListAppender<ILoggingEvent> appender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(ExceptionMapper.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(appender);
  }

  private HttpServletRequest request() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/species/x/1");
    return req;
  }

  @Test
  void unhandledErrorsAreLoggedAtError() {
    // an Error (not an Exception) must still be caught and logged - this is the NoClassDefFoundError case
    var response = mapper.handleException(new NoClassDefFoundError("boom"), request());

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(appender.list)
      .anyMatch(e -> e.getLevel() == Level.ERROR && e.getThrowableProxy() != null);
  }

  @Test
  void clientErrorsAreNotLoggedAtError() {
    mapper.handleNotFoundException(new NotFoundException("nope"), request());

    assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.ERROR);
  }

  @Test
  void springClientErrorsKeepTheirStatus() {
    // e.g. /taxon/suggest requested with an Accept header that excludes application/json
    var response = mapper.handleException(
      new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)), request());
    assertThat(response.getStatusCode().value()).isEqualTo(406);

    response = mapper.handleException(new HttpRequestMethodNotSupportedException("POST"), request());
    assertThat(response.getStatusCode().value()).isEqualTo(405);

    assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.ERROR);
  }
}
