package com.lifesetup.application;

import com.lifesetup.domain.*;
import com.lifesetup.integration.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfileExtractionService {
 private final ProfileExtractionClient client;
 public record Result(ProfileDraft draft,String mode) {}
 @org.springframework.beans.factory.annotation.Autowired
 public ProfileExtractionService() { this(configured()); }
 public ProfileExtractionService(ProfileExtractionClient client) { this.client=client; }
 private static ProfileExtractionClient configured() {
  String key=System.getenv("OPENAI_API_KEY"),model=System.getenv("OPENAI_MODEL");
  return key!=null&&!key.isBlank()&&model!=null&&!model.isBlank()?new ResponsesOpenAiClient(key,model):null;
 }
 public Result extract(String description) {
  if(description==null||description.isBlank()||description.length()>1200)throw new IllegalArgumentException("Describe your move in 1 to 1200 characters.");
  if(client!=null)try {
   var d=Objects.requireNonNull(client.extract(description));String lower=description.toLowerCase(Locale.ROOT);
   String nationality=matches(lower,"\\b(nationality|citizen|national|passport)\\b|\\bi(?: am|'m|’m) (?:a |an )?(?!already|moving|relocating|bringing|not|in |from |going|here)[a-z]+")?code(d.nationality(),false):null;
   String from=matches(lower,"\\b(mov(?:e|ed|ing)|relocat(?:e|ed|ing)|arriv(?:e|ed|ing)|came|coming) from\\b")?code(d.movingFrom(),false):null;
   String licence=matches(lower,"(?:licen[cs]e|permit).{0,30}(?:issued|from|country)|(?:have|hold|my).{0,35}(?:licen[cs]e|permit)|no foreign (?:driving )?licen[cs]e")?code(d.licenceCountry(),true):null;
   return new Result(new ProfileDraft(nationality,from,d.alreadyInUae(),d.movingWithFamily(),d.movingWithChildren(),d.residenceStatus(),d.hasEmiratesId(),d.hasHousing(),d.wantsToDrive(),licence,d.bringingPet()),"OPENAI");
  }catch(Exception ignored) { /* A failed interpretation never blocks onboarding. */ }
  return new Result(fallback(description),"FALLBACK");
 }
 private static String code(String value,boolean licence) { return Countries.validCode(value)||licence&&"NONE".equals(value)?value:null; }
 private static boolean matches(String text,String regex) { return Pattern.compile(regex).matcher(text).find(); }
 private static Boolean fact(String text,String yes,String no) { if(matches(text,no))return false;if(matches(text,yes))return true;return null; }
 private static String countryIn(String text) {
  return Countries.dataset().countries().stream().filter(c->{var names=new ArrayList<>(c.aliases());names.add(c.name());return names.stream().anyMatch(name->matches(text,"(?iu)^(?:a |an |the )?"+Pattern.quote(name)+"(?!\\p{L})"));})
   .max(Comparator.comparingInt(c->c.name().length())).map(Countries.Country::code).orElse(null);
 }
 private static String countryAfter(String text,String regex) { var m=Pattern.compile(regex).matcher(text);return m.find()?countryIn(m.group(1)):null; }
 public static ProfileDraft fallback(String description) {
  String text=description.toLowerCase(Locale.ROOT);
  String from=countryAfter(text,"(?:mov(?:e|ed|ing)|relocat(?:e|ed|ing)|arriv(?:e|ed|ing)|came|coming) from ([^.,;\\n]+)");
  String nationality=countryAfter(text,"(?:nationality (?:is )?|i(?: am|'m|’m) (?!already|moving|in |from |not))([^.,;\\n]+)");
  String licence=countryAfter(text,"(?:licen[cs]e|permit)(?: was| is)? (?:issued (?:in|by)|from) ([^.,;\\n]+)");
  if(licence==null)licence=countryAfter(text,"(?:have|hold|my) (?:a |an )?([^.,;\\n]+?)(?: driving)? licen[cs]e");
  if(matches(text,"no foreign (?:driving )?licen[cs]e|don.t have (?:a |any )?(?:foreign |driving )?licen[cs]e"))licence="NONE";
  Boolean children=fact(text,"(?:with|bringing).{0,50}(?:baby|child|children|kids)","no children|no kids|without children");
  Boolean family=fact(text,"with (?:my |a )?(?:wife|husband|spouse|family|partner|baby|child|children)","moving alone|on my own|without family");
  if(Boolean.TRUE.equals(children))family=true;
  UserProfile.ResidenceStatus residence=matches(text,"residence.{0,30}(?:not started|haven.t started)")?UserProfile.ResidenceStatus.NOT_STARTED:
   matches(text,"residence.{0,35}(?:being processed|in progress|underway|processing)")?UserProfile.ResidenceStatus.IN_PROGRESS:
   matches(text,"residence.{0,25}(?:complete|approved)")?UserProfile.ResidenceStatus.COMPLETE:null;
  return new ProfileDraft(nationality,from,fact(text,"already in (?:the )?(?:uae|abu dhabi)","not in (?:the )?(?:uae|abu dhabi)"),family,children,residence,
   fact(text,"(?:have|received).{0,12}emirates id","no emirates id|don.t have.{0,12}emirates id"),
   fact(text,"found a home|have (?:a home|housing)","no home yet|don.t have (?:a home|housing)|still looking for a home"),
   fact(text,"want to drive|will drive|plan to drive","don.t want to drive|won.t drive|will not drive"),licence,
   fact(text,"(?:with|bringing).{0,70}(?:cat|dog|pet)","no pets|without pets|not bringing.{0,10}(?:cat|dog|pet)"));
 }
}
