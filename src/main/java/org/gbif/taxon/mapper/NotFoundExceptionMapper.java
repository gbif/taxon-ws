package org.gbif.taxon.mapper;


import life.catalogue.api.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class NotFoundExceptionMapper {

  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity handleNotException(NotFoundException e) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .contentType(MediaType.TEXT_PLAIN)
      .body(e.getMessage());
  }
}
