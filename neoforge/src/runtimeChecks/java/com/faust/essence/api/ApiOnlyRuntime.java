package com.faust.essence.api;
@net.neoforged.fml.common.EventBusSubscriber(modid="essence_api")
public final class ApiOnlyRuntime {
 @net.neoforged.bus.api.SubscribeEvent
 public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event){
  try{if(EssenceApi.available()||Resources.stamina().isPresent()||Skill.values().length!=0||BonusEffects.known("damage"))throw new AssertionError("API must not install gameplay without Core");System.out.println("API-only dedicated server checks passed");}finally{event.getServer().halt(false);}
 }
}
