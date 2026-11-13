package com.faust.essence.api;
import java.util.*;
public final class Resources {
 private Resources(){}
 private static final class Providers {
  static final Map<String,ResourceProvider> ALL=load();
  static Map<String,ResourceProvider> load(){var result=new LinkedHashMap<String,ResourceProvider>();for(var p:ServiceLoader.load(ResourceProvider.class,ResourceProvider.class.getClassLoader()))if(result.putIfAbsent(Extensions.id(p.id()),p)!=null)throw new IllegalStateException("Duplicate resource "+p.id());return Map.copyOf(result);}
 }
 public static Optional<ResourceProvider> find(String id){return Optional.ofNullable(Providers.ALL.get(id));}
 public static Optional<ResourceProvider> stamina(){return find("fauststamina:stamina");}
 public static boolean payStamina(net.minecraft.server.level.ServerPlayer player,double amount,boolean exact){EssenceApi.requireServerThread(player);amount(amount);return stamina().map(provider->provider.pay(player,amount,exact)).orElse(true);}
 public static void amount(double amount){if(!Double.isFinite(amount)||amount<0)throw new IllegalArgumentException("Invalid resource amount");}
}
