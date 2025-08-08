package org.linn.woecrit.mixin.client;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.linn.woecrit.client.world.GhostChunk;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;" +
                    "Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;" +
                    "Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;" +
                    "Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At("TAIL"))
    private void createTwinGhostChunk(
            World world,
            ChunkPos pos,
            UpgradeData upgradeData,
            ChunkTickScheduler blockTickScheduler,
            ChunkTickScheduler fluidTickScheduler,
            long inhabitedTime,
            ChunkSection[] sectionArrayInitializer,
            WorldChunk.EntityLoader entityLoader,
            BlendingData blendingData,
            CallbackInfo ci) {
        var ghostWorld = GhostWorld.worldsTwinMap.get(world.getDimensionEntry());
        ghostWorld.chunksTwinMap.put(pos, new GhostChunk(pos));
    }
}
