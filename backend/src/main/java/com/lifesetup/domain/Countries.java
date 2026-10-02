package com.lifesetup.domain;

import java.text.Normalizer;
import java.util.*;
import tools.jackson.databind.json.JsonMapper;

public final class Countries {
 public record Country(String code,String name,List<String> aliases) {
  public Country { aliases=List.copyOf(aliases); }
 }
 public record Dataset(Map<String,String> source,List<Country> countries) {
  public Dataset { source=Map.copyOf(source);countries=List.copyOf(countries); }
 }
 private static final Dataset DATA=load();
 private Countries() {}
 private static Dataset load() {
  try(var input=Countries.class.getResourceAsStream("/countries.json")) {
   var data=JsonMapper.builder().build().readValue(input,Dataset.class);
   var codes=new HashSet<String>();
   for(var country:data.countries())if(!country.code().matches("[A-Z]{2}")||!codes.add(country.code()))throw new IllegalStateException("Invalid country dataset.");
   return data;
  }catch(Exception error){throw new IllegalStateException("Could not load country dataset.",error);}
 }
 public static Dataset dataset() { return DATA; }
 public static boolean validCode(String code) { return code!=null && DATA.countries().stream().anyMatch(c->c.code().equals(code)); }
 private static String key(String value) { return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).trim(); }
 public static String normalize(String value,boolean licence) {
  if(value==null||value.isBlank())return null;
  value=value.trim();
  if(licence&&value.equalsIgnoreCase("NONE"))return "NONE";
  String normalized=key(value);
  return DATA.countries().stream().filter(c->key(c.code()).equals(normalized)||key(c.name()).equals(normalized)||c.aliases().stream().anyMatch(a->key(a).equals(normalized)))
   .map(Country::code).findFirst().orElseThrow(()->new IllegalArgumentException("A country answer needs review. Choose a country or Unknown / not sure."));
 }
}
