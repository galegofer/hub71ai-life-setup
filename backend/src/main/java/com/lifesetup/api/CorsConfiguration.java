package com.lifesetup.api;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
 private final String[] origins;
 public CorsConfiguration(@Value("${CORS_ALLOWED_ORIGINS:http://127.0.0.1:4321,http://localhost:4321}") String configured) {
  origins=Arrays.stream(configured.split(",")).map(String::trim).filter(s->!s.isEmpty()).toArray(String[]::new);
  if(Arrays.stream(origins).anyMatch(s->s.contains("*") || !(s.startsWith("https://") || s.startsWith("http://"))))
   throw new IllegalArgumentException("CORS_ALLOWED_ORIGINS must contain exact HTTP(S) origins.");
 }
 @Override public void addCorsMappings(CorsRegistry registry) {
  registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods("GET","POST","PUT","OPTIONS")
   .allowedHeaders("Content-Type").allowCredentials(false).maxAge(600);
 }
}
