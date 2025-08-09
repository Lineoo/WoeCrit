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
import org.linn.woecrit.client.render.GhostRenderView;
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
    private void cooperateWithGhostWorld(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (Freecam.isEnabled() && GhostRender.isEnabled()) {
            var view = new GhostRenderView(ghostTwin);

            var block = cir.getReturnValue();

            if (block.isAir()) {
                cir.setReturnValue(view.getBlockState(pos));
            }
        }
    }
}
