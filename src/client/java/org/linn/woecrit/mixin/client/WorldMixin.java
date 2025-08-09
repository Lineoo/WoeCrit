package org.linn.woecrit.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.linn.woecrit.client.freecam.Freecam;
import org.linn.woecrit.client.render.GhostRender;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Unique
    private GhostWorld ghostTwin;

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
        this.ghostTwin = new GhostWorld((World)(Object) this);
        GhostWorld.worldsTwinMap.put(dimensionEntry, ghostTwin);
    }

    @Inject(method = "getBlockState", at = @At("TAIL"), cancellable = true)
    private void getMixedBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (Freecam.isEnabled() && GhostRender.isEnabled()) {
            var block = cir.getReturnValue();
            if (block.isAir()) {
                cir.setReturnValue(ghostTwin.getBlockState(pos));
            }
        }
    }

    @Inject(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void setGhostBlockState(
            BlockPos pos,
            BlockState state,
            int flags,
            int maxUpdateDepth,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // FIXME disable render in freecam will break it
        if (Freecam.isEnabled() && GhostRender.isEnabled()) {
            var success = ghostTwin.setBlockState(pos, state);
            cir.setReturnValue(success);
        }
    }
}
