package org.linn.woecrit.client.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

///  @see net.minecraft.world.chunk.WorldChunk
public class GhostChunk {

    private final Map<BlockPos, BlockState> blockStateMap = new HashMap<>();

    public GhostChunk(ChunkPos chunkPos) {}

    ///  @see net.minecraft.world.chunk.Chunk#getBlockState
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getY() == -40) {
            return Blocks.OAK_LOG.getDefaultState();
        }
        var blockState = blockStateMap.get(pos);
        return blockState != null ? blockState : Blocks.VOID_AIR.getDefaultState();
    }

    ///  @see net.minecraft.world.chunk.WorldChunk#setBlockState(BlockPos, BlockState, int)
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, int flags) {
        var blockState = blockStateMap.put(pos, state);
        return blockState != null ? blockState : Blocks.VOID_AIR.getDefaultState();
    }
}
