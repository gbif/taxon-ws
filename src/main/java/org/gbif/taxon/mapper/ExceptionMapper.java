package org.gbif.taxon.mapper;


import jakarta.servlet.http.HttpServletRequest;
import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.exception.SynonymException;
import org.gbif.taxon.api.ErrorMessage;
import org.gbif.taxon.dao.MissingGBIFKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionMapper {

  @ExceptionHandler(SynonymException.class)
  public ResponseEntity<?> handleSynonymException(SynonymException e, HttpServletRequest request) {
    String details = e.getMessage();
    if (e.acceptedKey != null) {
      details += ". Accepted taxon: " + e.acceptedKey.getId();
    }
    return respond(request, HttpStatus.NOT_FOUND, details);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
    return respond(request, HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
    return respond(request, HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(MissingGBIFKeyException.class)
  public ResponseEntity<?> handleMissingGBIFKeyException(MissingGBIFKeyException e, HttpServletRequest request) {
    return respond(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
    return respond(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ResponseEntity<?> respond(HttpServletRequest request, HttpStatus status, String details) {
    String accept = request.getHeader("Accept");
    if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
      return ResponseEntity
        .status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(status.value(), status.getReasonPhrase(), details));
    }
    return ResponseEntity
      .status(status)
      .contentType(MediaType.TEXT_PLAIN)
      .body(details);
  }
}
