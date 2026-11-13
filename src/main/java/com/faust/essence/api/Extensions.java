package com.faust.essence.api;
import java.util.*;
/** Immutable registration descriptors. Register during mod initialization. IDs must be namespaced. */
public final class Extensions {
 private Extensions(){}
 public static String id(String id){if(id==null||!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid extension ID: "+id);return id;}
 public record Requirement(Skill skill,int level){public Requirement{Objects.requireNonNull(skill);if(level<0)throw new IllegalArgumentException("Negative level");}}
 public record Condition(String counter,double amount){public Condition{Objects.requireNonNull(counter);if(!Double.isFinite(amount)||amount<0)throw new IllegalArgumentException("Invalid condition");}}
 public record Ability(String id,List<Requirement> requirements,List<Condition> conditions,List<String> parents,String effect,double value,boolean toggleable,double x,double y,String icon,List<String> anyParents,int requiredParents,List<String> excludes,int requiredConditions){
  public Ability{Extensions.id(id);requirements=List.copyOf(requirements);conditions=List.copyOf(conditions);parents=List.copyOf(parents);parents.forEach(Extensions::id);anyParents=List.copyOf(anyParents);anyParents.forEach(Extensions::id);excludes=List.copyOf(excludes);excludes.forEach(Extensions::id);if(requiredParents<0||requiredParents>anyParents.size()||requiredConditions<0||requiredConditions>conditions.size())throw new IllegalArgumentException("Invalid dependency counts");Objects.requireNonNull(effect);Objects.requireNonNull(icon);if(requirements.isEmpty()||!Double.isFinite(value)||!Double.isFinite(x)||!Double.isFinite(y))throw new IllegalArgumentException("Invalid ability");}
  public Ability(String id,List<Requirement> requirements,List<Condition> conditions,List<String> parents,String effect,double value,boolean toggleable,double x,double y,String icon){this(id,requirements,conditions,parents,effect,value,toggleable,x,y,icon,List.of(),0,List.of(),0);}
 }
 public record Action(String id,Skill skill,double experience,String counter){public Action{Extensions.id(id);Objects.requireNonNull(skill);Objects.requireNonNull(counter);if(!Double.isFinite(experience)||experience<0)throw new IllegalArgumentException("Invalid action XP");}}
 public record Tab(String namespace,double basePrice,double priceStep,boolean integrated){public Tab{if(namespace==null||!namespace.matches("[a-z0-9_.-]+")||!Double.isFinite(basePrice)||basePrice<0||!Double.isFinite(priceStep)||priceStep<0)throw new IllegalArgumentException("Invalid tab");}}
 public record Characteristic(Skill skill,double initialCap,double maximumCap,double capStep,double levelBase,double levelSlope,double maximumGrowth){
  public Characteristic{Objects.requireNonNull(skill);for(double n:new double[]{initialCap,maximumCap,capStep,levelBase,levelSlope,maximumGrowth})if(!Double.isFinite(n)||n<0)throw new IllegalArgumentException("Invalid characteristic rule");if(maximumCap<initialCap||maximumCap>20||capStep<=0||levelBase<=0||maximumGrowth<=0||maximumGrowth>2)throw new IllegalArgumentException("Invalid characteristic bounds");}
 }
 private static final Map<String,Characteristic> CHARACTERISTICS=new LinkedHashMap<>();
 public static void characteristic(Characteristic value){put(CHARACTERISTICS,value.skill().name(),value);EssenceApi.definitionsChanged();}
 public static synchronized List<Characteristic> characteristics(){return List.copyOf(CHARACTERISTICS.values());}
 private static final Map<String,Ability> ABILITIES=new LinkedHashMap<>();
 private static final Map<String,Action> ACTIONS=new LinkedHashMap<>();
 private static final Map<String,Tab> TABS=new LinkedHashMap<>();
 public static void ability(Ability ability){put(ABILITIES,ability.id(),ability);EssenceApi.definitionsChanged();}
 public static void action(Action action){put(ACTIONS,action.id(),action);EssenceApi.definitionsChanged();}
 public static void tab(Tab tab){put(TABS,tab.namespace(),tab);EssenceApi.definitionsChanged();}
 private static synchronized <T> void put(Map<String,T> values,String id,T value){if(values.putIfAbsent(id,value)!=null)throw new IllegalArgumentException("Duplicate extension: "+id);}
 public static synchronized List<Ability> abilities(){return List.copyOf(ABILITIES.values());}
 public static synchronized List<Action> actions(){return List.copyOf(ACTIONS.values());}
 public static synchronized List<Tab> tabs(){return List.copyOf(TABS.values());}
}
