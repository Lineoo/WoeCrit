package org.linn.woecrit.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.linn.woecrit.client.world.GhostWorld;

/// @see net.minecraft.client.render.chunk.ChunkRendererRegion
// TODO move to GhostWorld
public class GhostRenderView implements BlockRenderView {
    private GhostWorld world;
    private boolean originMixed;

    public GhostRenderView(GhostWorld world) {
        this(world, false);
    }

    public GhostRenderView(GhostWorld world, boolean originMixed) {
        this.world = world;
        this.originMixed = originMixed;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getManhattanDistance(new Vec3i(0, -56, 0)) < 5) {
            return Blocks.GLOWSTONE.getDefaultState();
        }

        var x = ChunkSectionPos.getSectionCoord(pos.getX());
        var z = ChunkSectionPos.getSectionCoord(pos.getZ());

//        if (originMixed) {
//            var twinChunk = world.twin.getChunk(x, z);
//            var block = twinChunk.getBlockState(pos);
//            if (!block.isAir()) {
//                return block;
//            }
//        }

        var chunk = world.chunksTwinMap.get(
                new ChunkPos(x, z));

        return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return world.twin.getLightingProvider();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return 0;
    }

    @Override
    public int getBottomY() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
