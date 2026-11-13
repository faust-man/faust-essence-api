package com.faust.essence.api;
import java.util.*;
public final class ApiContractTest {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static void rejects(Runnable action){try{action.run();}catch(IllegalArgumentException|UnsupportedOperationException expected){checks++;return;}throw new AssertionError("expected rejection");}
 public static void main(String[] args)throws Exception{
  check(Skill.values().length==0&&!BonusEffects.known("damage"),"API has no base gameplay definitions");
  check(!EssenceApi.available(),"API loads without Core");check(Resources.stamina().isEmpty(),"API loads without Stamina");
  var skill=EssenceApi.registerSkill("contract:skill");check(Skill.valueOf("contract:skill")==skill,"stable identity");rejects(()->EssenceApi.registerSkill("contract:skill"));rejects(()->EssenceApi.registerSkill("bad"));
  var mutable=new HashSet<String>();mutable.add("contract:ability");var snapshot=new PlayerSnapshot(Map.of(),mutable,Set.of(),"");mutable.clear();check(snapshot.learned().size()==1,"snapshot detached");rejects(()->snapshot.learned().clear());
  Extensions.tab(new Extensions.Tab("contract",1,1,false));rejects(()->Extensions.tab(new Extensions.Tab("contract",2,1,false)));rejects(()->new Extensions.Tab("bad:id",1,1,false));
  Extensions.characteristic(new Extensions.Characteristic(skill,3,20,1,100,20,2));rejects(()->new Extensions.Characteristic(skill,3,2,1,100,20,2));
  var requirements=new ArrayList<Extensions.Requirement>();requirements.add(new Extensions.Requirement(skill,1));
  Extensions.ability(new Extensions.Ability("contract:ability",requirements,List.of(new Extensions.Condition("contract:uses",2)),List.of(),"contract:effect",1,true,0,0,"minecraft:stone"));requirements.clear();check(Extensions.abilities().getFirst().requirements().size()==1,"descriptor detached");
  Extensions.action(new Extensions.Action("contract:use",skill,1,"contract:uses"));rejects(()->Extensions.action(new Extensions.Action("contract:use",skill,1,"contract:uses")));rejects(()->Resources.amount(Double.NaN));rejects(()->Resources.amount(-1));
  var state=new java.util.concurrent.atomic.AtomicInteger();EssenceApi.onStateChange(change->state.incrementAndGet());EssenceApi.stateChanged(new EssenceApi.StateChange(UUID.randomUUID(),snapshot,snapshot));check(state.get()==1,"state event");
  var reload=new java.util.concurrent.atomic.AtomicInteger();EssenceApi.onConfigurationReload(reload::incrementAndGet);EssenceApi.configurationReloaded();check(reload.get()==1,"reload event");
  EssenceApi.beforeProgress(grant->.5);check(EssenceApi.before(new EssenceApi.Grant(null,"contract:use",1,1))==.5,"before-progress hook");
  var registrationError=new java.util.concurrent.atomic.AtomicReference<Throwable>();
  var threads=new ArrayList<Thread>();for(int worker=0;worker<2;worker++){final int index=worker;threads.add(new Thread(()->{try{for(int i=0;i<100;i++)Extensions.tab(new Extensions.Tab("parallel_"+index+"_"+i,1,1,false));}catch(Throwable error){registrationError.set(error);}}));}
  for(var thread:threads)thread.start();for(var thread:threads)thread.join();check(registrationError.get()==null&&Extensions.tabs().size()==201,"parallel mod registration preserves every definition");
  System.out.println("API independent contract checks passed: "+checks);
 }
}
