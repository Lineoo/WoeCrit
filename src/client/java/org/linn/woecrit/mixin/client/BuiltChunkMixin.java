package org.linn.woecrit.mixin.client;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.linn.woecrit.client.render.GhostBuiltChunk;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkMixin {
    @Shadow
    @Final
    private BlockPos.Mutable origin;

    @Unique
    private GhostWorld recordedGhostWorld;

    @Unique
    private GhostBuiltChunk recordedGhostBuiltChunk = new GhostBuiltChunk();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createTwinGhostSection(ChunkBuilder chunkBuilder, int index, long sectionPos, CallbackInfo ci) {
        var world = ((ChunkBuildertAccessor) chunkBuilder).getWorld();
        var ghostWorld = GhostWorld.worldsTwinMap.get(world.getDimensionEntry());
        recordedGhostWorld = ghostWorld;
        recordedGhostWorld.render.blockRender.build(recordedGhostBuiltChunk);
        ghostWorld.render.blockRender.builtChunksTwinMap.add(recordedGhostBuiltChunk);
    }

    @Inject(method = "createRebuildTask", at = @At("HEAD"))
    void rebuildTwinGhostSection(ChunkRendererRegionBuilder builder, CallbackInfoReturnable<ChunkBuilder.BuiltChunk.Task> cir) {
        recordedGhostWorld.render.blockRender.build(recordedGhostBuiltChunk);
    }

    @Inject(method = "setSectionPos", at = @At("TAIL"))
    void setTwinGhostSectionPos(long sectionPos, CallbackInfo ci) {
        recordedGhostBuiltChunk.sectionPos = ChunkSectionPos.from(sectionPos);
        recordedGhostBuiltChunk.origin = this.origin;
    }

    @Mixin(ChunkBuilder.class)
    interface ChunkBuildertAccessor {
        @Accessor ClientWorld getWorld();
    }
}
