package org.linn.woecrit.mixin.client;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void createTwinGhostWorld(
            MutableWorldProperties properties,
            RegistryKey registryRef,
            DynamicRegistryManager registryManager,
            RegistryEntry dimensionEntry,
            boolean isClient,
            boolean debugWorld,
            long seed,
            int maxChainedNeighborUpdates,
            CallbackInfo ci
    ) {
        GhostWorld.worldsTwinMap.put(dimensionEntry, new GhostWorld());
    }
}
