package com.faust.essence.api;




import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import java.util.function.*;

/** Server-thread API v1. Register extensions during mod initialization. */
public final class EssenceApi {
    private EssenceApi() {}
    public record StateChange(UUID player,PlayerSnapshot before,PlayerSnapshot after){}
    private static final List<Consumer<StateChange>> STATE=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Runnable> RELOAD=new java.util.concurrent.CopyOnWriteArrayList<>();
    public static void onStateChange(Consumer<StateChange> listener){STATE.add(Objects.requireNonNull(listener));}
    public static boolean hasStateListeners(){return !STATE.isEmpty();}
    public static void stateChanged(StateChange change){for(var listener:List.copyOf(STATE))listener.accept(change);}
    public static void onConfigurationReload(Runnable listener){RELOAD.add(Objects.requireNonNull(listener));}
    public static void configurationReloaded(){for(var listener:List.copyOf(RELOAD))listener.run();}
    public enum DefenseKind { SHIELD_BLOCK, WEAPON_BLOCK, PARRY, DODGE }
    public record Grant(ServerPlayer player, String action, double units, double count) {}
    public record Change(UUID player, String action, PlayerSnapshot before, PlayerSnapshot after) {}
    private static final List<ToDoubleFunction<Grant>> BEFORE=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Consumer<Change>> AFTER=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final Map<String,Predicate<ServerPlayer>> PATHS=new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String,java.util.function.BiConsumer<ServerPlayer,Double>> BONUSES=new java.util.concurrent.ConcurrentHashMap<>();
    public static Skill registerSkill(String id) {if(!id.contains(":"))throw new IllegalArgumentException("Extension skill requires namespace");return Skill.register(id);}
    public static void registerBonus(String id,java.util.function.BiConsumer<ServerPlayer,Double> handler) {
        Objects.requireNonNull(handler);Extensions.id(id);BonusEffects.register(id);BONUSES.put(id,handler);
    }
    public static void applyBonuses(ServerPlayer player) {
        for(var bonus:BONUSES.entrySet())bonus.getValue().accept(player,backend().effect(player,bonus.getKey()));
    }
    public static void beforeProgress(ToDoubleFunction<Grant> handler) {BEFORE.add(Objects.requireNonNull(handler));}
    public static void afterProgress(Consumer<Change> handler) {AFTER.add(Objects.requireNonNull(handler));}
    public static boolean hasAfterListeners(){return !AFTER.isEmpty();}
    public static boolean needsCounter(ServerPlayer player,String key){requireServerThread(player);return backend().needsCounter(player,key);}
    public static boolean recordCounter(ServerPlayer player,String key,double amount){requireServerThread(player);return backend().active(player)&&backend().recordCounter(player,key,amount,false);}
    public static boolean recordMaximum(ServerPlayer player,String key,double value){requireServerThread(player);return backend().active(player)&&backend().recordCounter(player,key,value,true);}
    public static void registerPath(String id,Predicate<ServerPlayer> requirement) {
        if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||PATHS.putIfAbsent(id,Objects.requireNonNull(requirement))!=null) throw new IllegalArgumentException("Invalid/duplicate path "+id);
    }
    public static Set<String> paths() {return Set.copyOf(PATHS.keySet());}
    public static boolean choosePath(ServerPlayer player,String id) {
        requireServerThread(player);
        var requirement=PATHS.get(id);return requirement!=null&&requirement.test(player)&&backend().choosePath(player,id);
    }
    public static PlayerSnapshot snapshot(ServerPlayer player){requireServerThread(player);return backend().snapshot(player);}
    public static boolean active(ServerPlayer player){requireServerThread(player);return backend().active(player);}
    public static boolean meets(ServerPlayer player,Skill skill,int level){return snapshot(player).tracks().get(skill).level()>=level;}
    public static boolean grantAction(ServerPlayer player,String action,double units,double count){requireServerThread(player);return backend().grantAction(player,action,units,count);}
    public static void grantExperience(ServerPlayer player,Skill skill,double amount){requireServerThread(player);Resources.amount(amount);backend().grantExperience(player,skill,amount);}
    public static double effect(ServerPlayer player,String effect){requireServerThread(player);return backend().effect(player,effect);}
    public static boolean learned(ServerPlayer player,String id){requireServerThread(player);return backend().learned(player,id);}
    public static boolean enabled(ServerPlayer player,String id){requireServerThread(player);return backend().enabled(player,id);}
    public static boolean unlock(ServerPlayer player,String id){requireServerThread(player);return backend().unlock(player,id);}
    public static boolean toggle(ServerPlayer player,String id){requireServerThread(player);return backend().toggle(player,id);}
    public static boolean buyCap(ServerPlayer player,Skill skill){requireServerThread(player);return backend().buyCap(player,skill);}
    private static final class Provider {
        static final Optional<EssenceBackend> INSTANCE=load();
        static Optional<EssenceBackend> load(){var all=ServiceLoader.load(EssenceBackend.class,EssenceBackend.class.getClassLoader()).stream().toList();if(all.size()>1)throw new IllegalStateException("Multiple Essence backends");return all.stream().findFirst().map(ServiceLoader.Provider::get);}
    }
    public static boolean available(){return Provider.INSTANCE.isPresent();}
    private static EssenceBackend backend(){return Provider.INSTANCE.orElseThrow(()->new IllegalStateException("No Essence progression backend installed"));}
    static void definitionsChanged(){Provider.INSTANCE.ifPresent(EssenceBackend::definitionsChanged);}
    public static double before(Grant grant) {
        double scale=1;
        for(var handler:List.copyOf(BEFORE)) {double value=handler.applyAsDouble(grant);if(!Double.isFinite(value)||value<0||value>100) throw new IllegalArgumentException("Invalid progress multiplier");scale*=value;}
        return Math.min(100,scale);
    }
    public static void after(Change change) {for(var handler:List.copyOf(AFTER)) handler.accept(change);}
    public static void requireServerThread(ServerPlayer player) {
        if(player.getServer()==null||!player.getServer().isSameThread()) throw new IllegalStateException("Core API requires server thread");
    }
}
