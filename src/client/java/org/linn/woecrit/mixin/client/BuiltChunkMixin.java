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

    @Shadow
    @Final
    public int index;

    @Unique
    private GhostWorld world;

    @Unique
    private GhostBuiltChunk twin = new GhostBuiltChunk();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createTwinGhostSection(ChunkBuilder chunkBuilder, int index, long sectionPos, CallbackInfo ci) {
        var world = ((ChunkBuilderAccessor) chunkBuilder).getWorld();
        var ghostWorld = GhostWorld.worldsTwinMap.get(world.getDimensionEntry());
        this.world = ghostWorld;

        ghostWorld.render.blockRender.builtChunksTwinMap.put(index, twin);
        this.world.render.blockRender.build(twin);
    }

    @Inject(method = "createRebuildTask", at = @At("HEAD"))
    void rebuildTwinGhostSection(ChunkRendererRegionBuilder builder, CallbackInfoReturnable<ChunkBuilder.BuiltChunk.Task> cir) {
        world.render.blockRender.build(twin);
    }

    @Inject(method = "setSectionPos", at = @At("TAIL"))
    void setTwinGhostSectionPos(long sectionPos, CallbackInfo ci) {
        twin.sectionPos = ChunkSectionPos.from(sectionPos);
        twin.origin = this.origin;
    }

    @Mixin(ChunkBuilder.class)
    interface ChunkBuilderAccessor {
        @Accessor ClientWorld getWorld();
    }
}
