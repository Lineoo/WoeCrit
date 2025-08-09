package org.linn.woecrit.client.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.linn.woecrit.client.render.GhostRender;

import java.util.HashMap;
import java.util.Map;

/// Only contains GhostData. Use `twin` if needed both.
/// @see net.minecraft.world.World
public class GhostWorld implements BlockRenderView {
    public static final Map<RegistryEntry<DimensionType>, GhostWorld> worldsTwinMap = new HashMap<>();

    // Careful about memory leak.
    public final Map<ChunkPos, GhostChunk> chunksTwinMap = new HashMap<>();
    public final GhostRender render;

    public final World twin;

    public GhostWorld(World twin) {
        this.twin = twin;
        this.render = new GhostRender(this);
    }

    public GhostChunk getChunk(BlockPos pos) {
        return this.getChunk(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    public GhostChunk getChunk(int x, int z) {
        return chunksTwinMap.get(new ChunkPos(x, z));
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    public BlockState getBlockState(BlockPos pos) {
        if (pos.getManhattanDistance(new Vec3i(0, -56, 0)) < 5) {
            return Blocks.GLOWSTONE.getDefaultState();
        }

        var x = ChunkSectionPos.getSectionCoord(pos.getX());
        var z = ChunkSectionPos.getSectionCoord(pos.getZ());

        var chunk = chunksTwinMap.get(
                new ChunkPos(x, z));

        return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }

    /// @see net.minecraft.world.World#setBlockState(BlockPos, BlockState, int)
    public boolean setBlockState(BlockPos pos, BlockState targetState) {
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        }

        GhostChunk chunk = this.getChunk(pos);

        BlockState prevBlockState = chunk.setBlockState(pos, targetState);

        if (prevBlockState != targetState) {
            // rebuild chunk
            render.blockRender.rebuild(pos);
        }

        return true;
    }

    @Override
    public int getHeight() {
        return twin.getHeight();
    }

    @Override
    public int getBottomY() {
        return twin.getBottomY();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return twin.getLightingProvider();
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return 0;
    }
}
