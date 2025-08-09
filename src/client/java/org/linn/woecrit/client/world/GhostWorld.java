package org.linn.woecrit.client.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.dimension.DimensionType;
import org.linn.woecrit.client.render.GhostRender;

import java.util.HashMap;
import java.util.Map;

/// @see net.minecraft.world.World
public class GhostWorld {
    public static final Map<RegistryEntry<DimensionType>, GhostWorld> worldsTwinMap = new HashMap<>();

    // Careful about memory leak.
    public final Map<ChunkPos, GhostChunk> chunksTwinMap = new HashMap<>();
    public final GhostRender render = new GhostRender(this);

    public GhostChunk getChunk(BlockPos pos) {
        return this.getChunk(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    public GhostChunk getChunk(int x, int z) {
        return chunksTwinMap.get(new ChunkPos(x, z));
    }

    public BlockState getBlockState(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        }

        var worldChunk = this.getChunk(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ()));
        return worldChunk.getBlockState(pos);
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

    private boolean isOutOfHeightLimit(BlockPos pos) {
        return false;
    }
}
