package com.faust.essence.api;
import net.minecraft.server.level.ServerPlayer;
/** Implemented by the progression engine; discovered lazily, never required to load the API. */
public interface EssenceBackend {
 PlayerSnapshot snapshot(ServerPlayer player);
 boolean active(ServerPlayer player);
 boolean grantAction(ServerPlayer player,String action,double units,double count);
 void grantExperience(ServerPlayer player,Skill skill,double amount);
 boolean needsCounter(ServerPlayer player,String counter);
 boolean recordCounter(ServerPlayer player,String counter,double amount,boolean maximum);
 double effect(ServerPlayer player,String effect);
 boolean learned(ServerPlayer player,String ability);
 boolean enabled(ServerPlayer player,String ability);
 boolean unlock(ServerPlayer player,String ability);
 boolean toggle(ServerPlayer player,String ability);
 boolean buyCap(ServerPlayer player,Skill skill);
 boolean choosePath(ServerPlayer player,String path);
 void definitionsChanged();
}
