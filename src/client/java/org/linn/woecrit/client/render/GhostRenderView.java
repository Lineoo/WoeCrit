package org.linn.woecrit.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class GhostRenderView implements BlockRenderView {

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getManhattanDistance(new Vec3i(0, -56, 0)) < 5) {
            return Blocks.GLOWSTONE.getDefaultState();
        }
        return Blocks.AIR.getDefaultState();
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
        return LightingProvider.DEFAULT;
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
