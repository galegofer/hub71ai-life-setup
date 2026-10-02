package com.lifesetup.application;
import com.lifesetup.domain.*;
import java.time.LocalDate;
import java.util.*;
public final class TaskCatalogue {
 private static OfficialSource source(String authority,String title,String url,String scope) {
  return new OfficialSource(authority,title,url,LocalDate.of(2026,10,2),scope);
 }
 private static final OfficialSource ICP=source("ICP","Residence and identity services","https://icp.gov.ae/en/","Official authority link. Requirements have not been reviewed for this prototype.");
 private static final OfficialSource ID=source("ICP","Check your ID card status","https://icp.gov.ae/en/id-card-status/","Official status page link. This app does not check your application.");
 private static final OfficialSource TAMM=source("TAMM","Abu Dhabi government services","https://www.tamm.abudhabi/","Official portal link. Find and confirm the relevant service there.");
 private static final OfficialSource DRIVE=source("TAMM","Replace a foreign driving licence","https://www.tamm.abudhabi/wb/itc/driver-services/replace-foreign-license/login","Official service link. Exchange eligibility has not been verified for your circumstances.");
 private static final OfficialSource RENT=source("TAMM","Tawtheeq tenancy services","https://www.tamm.abudhabi/wb/adm/tawtheeq-dashboard/home?lang=en","Official tenancy portal link.");
 private static final OfficialSource WATER=source("ADDC","How to connect water and electricity","https://www.addc.ae/en-us/residential/pages/howtoconnect.aspx","The official page says Tawtheeq registration sets up accounts for most renters in Abu Dhabi city and surrounding areas. Confirm your own property arrangements.");
 public static List<TaskDefinition> all() {
  return List.of(
   new TaskDefinition("residence","Finish your residence process","Check with your employer about the next step for your move.",List.of(),List.of("Ask your employer which documents they need."),ICP,"Check official information","Prototype sequence. Your employer may manage several steps together."),
   new TaskDefinition("emirates-id","Get your Emirates ID","Your Emirates ID is your UAE identity card. Check how your application is progressing.",List.of("residence"),List.of("Check your application details with your employer or ICP."),ID,"Check your ID status","You can receive the card while the related steps are managed together. Recording receipt also confirms residence completion in this prototype."),
   new TaskDefinition("housing","Find a home","Choose a home that works for your commute and your family.",List.of(),List.of("Decide your budget and preferred area."),null,"View your next action","Speak with a landlord or agent. This app does not arrange housing."),
   new TaskDefinition("tawtheeq","Check your rental registration","Tawtheeq is Abu Dhabi’s rental contract register. Ask whether your contract has been registered.",List.of("housing"),List.of("Ask your landlord or agent about your contract registration."),RENT,"Open tenancy services","Prototype dependency: choose your home before checking its rental registration."),
   new TaskDefinition("utilities","Confirm electricity and water","Check that your water and electricity accounts are set up for your home.",List.of("tawtheeq"),List.of("Check your account details after your rental registration."),WATER,"Read connection information","An account may be created automatically through Tawtheeq. Do not assume you need a separate application."),
   new TaskDefinition("bank","Open a bank account","Compare banks and ask your chosen bank what it needs.",List.of("emirates-id"),List.of("Confirm the bank’s documents and account options directly."),null,"Contact your chosen bank","Emirates ID is a prototype planning dependency. Banks may offer different routes and account types."),
   new TaskDefinition("driving","Check your driving licence options","Find out whether you can exchange your current licence or need another route.",List.of("emirates-id"),List.of("Confirm your licence country, documents and exchange options with the official service."),DRIVE,"Open official service","Ready means you can check the next step. It does not confirm that you qualify for a licence exchange."),
   new TaskDefinition("family","Plan your family’s move","Check which residence and identity steps your family members need.",List.of(),List.of("Check each family member’s situation with the official authority."),ICP,"Check official information","This prototype provides a starting point, not a family residence decision."),
   new TaskDefinition("school","Explore school or nursery options","Find suitable places and ask about availability and enrolment.",List.of(),List.of("Ask each school or nursery about places and documents."),TAMM,"Explore official information","You can research options now. This app does not determine enrolment rules."),
   new TaskDefinition("pet","Plan your pet’s move","Check the official rules before arranging your pet’s travel.",List.of(),List.of("Find the relevant pet import service and confirm its current rules."),TAMM,"Find official information","Pet import requirements have not been reviewed for this prototype. This link opens the general government portal."),
   new TaskDefinition("insurance","Check your health insurance","Ask your employer about your cover and your family’s cover.",List.of(),List.of("Ask when your cover starts and who is covered."),TAMM,"Find official information","This app does not confirm your insurance obligations or coverage.")
  ).stream().map(TaskCatalogue::metadata).toList();
 }
 private static TaskDefinition metadata(TaskDefinition t) {
  var order=List.of("residence","emirates-id","housing","tawtheeq","utilities","insurance","bank","driving","family","school","pet");
  String category=switch(t.id()){case "residence","emirates-id"->"IDENTITY";case "housing","tawtheeq","utilities"->"HOME";case "bank"->"MONEY";case "driving"->"TRANSPORT";case "family","school"->"FAMILY";case "pet"->"PET";case "insurance"->"HEALTH";default->"GENERAL";};
  String applies=switch(t.id()){case "family"->"MOVING_WITH_FAMILY";case "school"->"MOVING_WITH_CHILDREN";case "driving"->"WANTS_TO_DRIVE";case "pet"->"BRINGING_PET";default->"ALWAYS";};
  String label=switch(t.id()){case "residence"->"Residence completed";case "emirates-id"->"Emirates ID received";case "housing"->"I found a home";case "insurance"->"Coverage confirmed";case "driving"->"I checked my options";default->"Mark this step done";};
  return new TaskDefinition(t.id(),t.title(),t.summary(),t.dependencies(),t.requirements(),t.officialSource(),t.nextAction(),t.note(),category,applies,order.size()-order.indexOf(t.id()),label);
 }
 public static TaskDefinition find(String id) {
  return all().stream().filter(t->t.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("That task does not exist."));
 }
 private TaskCatalogue() {}
}
