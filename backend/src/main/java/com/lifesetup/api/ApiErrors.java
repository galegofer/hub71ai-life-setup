package com.lifesetup.api;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.*;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<Map<String,String>> missing(NoSuchElementException e) { return ResponseEntity.status(404).body(Map.of("message",e.getMessage())); }
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("message",e.getMessage())); }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentNotValidException.class}) public ResponseEntity<Map<String,String>> invalidBody(Exception e) { return ResponseEntity.badRequest().body(Map.of("message","Check your answers and try again.")); }
}
