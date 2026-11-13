package com.faust.essence.api.client;
import com.faust.essence.api.*;
import java.util.Optional;
/** Loaded only by an explicit client query; never discovered by server-side API code. */
public interface ClientBackend {
 Optional<PlayerSnapshot> snapshot();
 Optional<ResourceProvider.ResourceState> stamina();
 boolean resourceEnabled();
}
