package org.linn.woecrit.client.render;

import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.*;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;
import net.minecraft.world.BlockRenderView;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GhostBlockRender {
    private final BlockRenderManager blockRenderManager;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public ChunkRenderData chunkRenderData;
    private ChunkSectionPos sectionPos = ChunkSectionPos.from(1, -40, 1);

    public Vec3d cameraPosition = Vec3d.ZERO;

    private BlockBufferAllocatorStorage allocatorStorage = new BlockBufferAllocatorStorage();

    public GhostBlockRender(
            BlockRenderManager blockRenderManager,
            BlockEntityRenderDispatcher blockEntityRenderDispatcher
    ) {
        this.blockRenderManager = blockRenderManager;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
    }

    public void build_new() {
        // Custom provide source
        GhostRenderView renderRegion = new GhostRenderView();
        VertexSorter vertexSorter = VertexSorter.byDistance(
                (float)(cameraPosition.x - sectionPos.getMinX()),
                (float)(cameraPosition.y - sectionPos.getMinY()),
                (float)(cameraPosition.z - sectionPos.getMinZ()));

        SectionBuilder.RenderData renderData = new SectionBuilder.RenderData();
        BlockPos blockPos = sectionPos.getMinPos();
        BlockPos blockPos2 = blockPos.add(15, 15, 15);
        ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
        MatrixStack matrixStack = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Map<BlockRenderLayer, BufferBuilder> map = new EnumMap(BlockRenderLayer.class);
        Random random = Random.create();
        List<BlockModelPart> list = new ObjectArrayList<>();

        for (BlockPos blockPos3 : BlockPos.iterate(blockPos, blockPos2)) {
            BlockState blockState = renderRegion.getBlockState(blockPos3);
            if (blockState.isOpaqueFullCube()) {
                chunkOcclusionDataBuilder.markClosed(blockPos3);
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                BlockRenderLayer blockRenderLayer = RenderLayers.getFluidLayer(fluidState);
                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, blockRenderLayer);
                this.blockRenderManager.renderFluid(blockPos3, renderRegion, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderType() == BlockRenderType.MODEL) {
                BlockRenderLayer blockRenderLayer = RenderLayers.getBlockLayer(blockState);
                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, blockRenderLayer);
                random.setSeed(blockState.getRenderingSeed(blockPos3));
                this.blockRenderManager.getModel(blockState).addParts(random, list);
                matrixStack.push();
                matrixStack.translate(
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getX()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getY()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos3.getZ())
                );
                this.blockRenderManager.renderBlock(blockState, blockPos3, renderRegion, matrixStack, bufferBuilder, true, list);
                matrixStack.pop();
                list.clear();
            }
        }

        for (Map.Entry<BlockRenderLayer, BufferBuilder> entry : map.entrySet()) {
            BlockRenderLayer blockRenderLayer2 = (BlockRenderLayer)entry.getKey();
            BuiltBuffer builtBuffer = ((BufferBuilder)entry.getValue()).endNullable();
            if (builtBuffer != null) {
                if (blockRenderLayer2 == BlockRenderLayer.TRANSLUCENT) {
                    renderData.translucencySortingData = builtBuffer.sortQuads(allocatorStorage.get(blockRenderLayer2), vertexSorter);
                }

                renderData.buffers.put(blockRenderLayer2, builtBuffer);
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        renderData.chunkOcclusionData = chunkOcclusionDataBuilder.build();

        NormalizedRelativePos normalizedRelativePos =
                NormalizedRelativePos.of(this.cameraPosition, sectionPos.asLong());
        chunkRenderData = new ChunkRenderData(normalizedRelativePos, renderData);
        uploadLayer(renderData.buffers, chunkRenderData);
    }

    private void uploadLayer(Map<BlockRenderLayer, BuiltBuffer> buffersByLayer, ChunkRenderData renderData) {
        buffersByLayer.forEach((layer, buffer) -> {
            ScopedProfiler scopedProfiler = Profilers.get().scoped("Upload Section Layer");

            try {
                renderData.upload(layer, buffer, this.sectionPos.asLong());
                buffer.close();
            } catch (Throwable var8) {
                if (scopedProfiler != null) {
                    try {
                        scopedProfiler.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (scopedProfiler != null) {
                scopedProfiler.close();
            }
        });
    }

    private BufferBuilder beginBufferBuilding(
            Map<BlockRenderLayer, BufferBuilder> builders,
            BlockBufferAllocatorStorage allocatorStorage,
            BlockRenderLayer layer
    ) {
        BufferBuilder bufferBuilder = (BufferBuilder)builders.get(layer);
        if (bufferBuilder == null) {
            BufferAllocator bufferAllocator = allocatorStorage.get(layer);
            bufferBuilder = new BufferBuilder(
                    bufferAllocator,
                    VertexFormat.DrawMode.QUADS,
                    VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            builders.put(layer, bufferBuilder);
        }

        return bufferBuilder;
    }

}
