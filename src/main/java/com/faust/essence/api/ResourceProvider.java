package com.faust.essence.api;
import net.minecraft.server.level.ServerPlayer;
/** Server-side optional resource service. Implementations own storage and payment rules. */
public interface ResourceProvider {
 String id();
 ResourceState state(ServerPlayer player);
 boolean canSpend(ServerPlayer player,double amount);
 /** Returns the amount actually spent; final fractional payments follow provider rules. */
 double spend(ServerPlayer player,double amount);
 /** Atomic action payment; exact forbids consuming a fractional remainder. */
 boolean pay(ServerPlayer player,double amount,boolean exact);
 void restore(ServerPlayer player,double amount);
 record ResourceState(double current,double maximum,double fatigue,boolean exhausted){}
}
