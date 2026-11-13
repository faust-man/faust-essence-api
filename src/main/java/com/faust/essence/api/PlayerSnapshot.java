package com.faust.essence.api;
import java.util.*;
/** Detached immutable view. No persistence schema or implementation classes. */
public record PlayerSnapshot(Map<Skill,Track> tracks,Set<String> learned,Set<String> disabled,String path){
 public record Track(int level,double experience,double required,double cap,long price,double growth){}
 public PlayerSnapshot{tracks=Map.copyOf(tracks);learned=Set.copyOf(learned);disabled=Set.copyOf(disabled);Objects.requireNonNull(path);}
}
