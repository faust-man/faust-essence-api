package com.faust.essence.api.client;
import com.faust.essence.api.*;
import java.util.*;
/** Read-only synchronized client view. Server-side mutations stay in EssenceApi. */
public final class ClientApi {
 private ClientApi(){}
 private static final class Provider {static final Optional<ClientBackend> VALUE=ServiceLoader.load(ClientBackend.class,ClientBackend.class.getClassLoader()).findFirst();}
 public static Optional<PlayerSnapshot> snapshot(){return Provider.VALUE.flatMap(ClientBackend::snapshot);}
 public static Optional<ResourceProvider.ResourceState> stamina(){return Provider.VALUE.flatMap(ClientBackend::stamina);}
 public static boolean resourceEnabled(){return Provider.VALUE.map(ClientBackend::resourceEnabled).orElse(false);}
 public static boolean enabled(String ability){return snapshot().map(p->p.learned().contains(ability)&&!p.disabled().contains(ability)).orElse(false);}
}
