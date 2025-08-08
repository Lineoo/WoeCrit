package org.linn.woecrit.mixin.client;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRendererRegionBuilder.class)
public class ChunkRendererRegionBuilderMixin {
    @Inject(method = "build", at = @At("TAIL"))
    private void pairGhostChunkRender(World world, long sectionPos, CallbackInfoReturnable<ChunkRendererRegion> cir) {
        var chunkRendererRegion = cir.getReturnValue();
    }
}
