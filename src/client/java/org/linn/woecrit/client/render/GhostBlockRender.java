package org.linn.woecrit.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
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
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.linn.woecrit.client.world.GhostWorld;

import java.util.*;

public class GhostBlockRender {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final BlockRenderManager blockRenderManager = client.getBlockRenderManager();

    public Vec3d cameraPosition = Vec3d.ZERO;
    private final BlockBufferAllocatorStorage allocatorStorage = new BlockBufferAllocatorStorage();

    public final Map<Integer, GhostBuiltChunk> builtChunksTwinMap = new HashMap<>();
    public BuiltChunkStorage builtChunkStorage;
    public final GhostWorld world;

    public GhostBlockRender(GhostWorld world) {
        this.world = world;
    }

    public void rebuild(BlockPos blockPos) {
        if (builtChunkStorage == null) {
            return;
        }
        builtChunkStorage.scheduleRebuild(
                ChunkSectionPos.getSectionCoord(blockPos.getX()),
                ChunkSectionPos.getSectionCoord(blockPos.getY()),
                ChunkSectionPos.getSectionCoord(blockPos.getZ()),
                false);
    }

    ///  @see SectionBuilder#build
    public void build(GhostBuiltChunk chunk) {
        // Custom provide source
        VertexSorter vertexSorter = VertexSorter.byDistance(
                (float)(cameraPosition.x - chunk.sectionPos.getMinX()),
                (float)(cameraPosition.y - chunk.sectionPos.getMinY()),
                (float)(cameraPosition.z - chunk.sectionPos.getMinZ()));

        SectionBuilder.RenderData renderData = new SectionBuilder.RenderData();
        BlockPos blockFrom = chunk.sectionPos.getMinPos();
        BlockPos blockTo = blockFrom.add(15, 15, 15);
        ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
        MatrixStack matrixStack = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Map<BlockRenderLayer, BufferBuilder> map = new EnumMap(BlockRenderLayer.class);
        Random random = Random.create();
        List<BlockModelPart> list = new ObjectArrayList<>();

        for (BlockPos blockPos : BlockPos.iterate(blockFrom, blockTo)) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOpaqueFullCube()) {
                chunkOcclusionDataBuilder.markClosed(blockPos);
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                BlockRenderLayer blockRenderLayer = RenderLayers.getFluidLayer(fluidState);
                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, blockRenderLayer);
                this.blockRenderManager.renderFluid(blockPos, world, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderType() == BlockRenderType.MODEL) {
                BlockRenderLayer blockRenderLayer = RenderLayers.getBlockLayer(blockState);
                BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, blockRenderLayer);
                random.setSeed(blockState.getRenderingSeed(blockPos));
                this.blockRenderManager.getModel(blockState).addParts(random, list);
                matrixStack.push();
                matrixStack.translate(
                        (float)ChunkSectionPos.getLocalCoord(blockPos.getX()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos.getY()),
                        (float)ChunkSectionPos.getLocalCoord(blockPos.getZ())
                );
                this.blockRenderManager.renderBlock(blockState, blockPos, world, matrixStack, bufferBuilder, true, list);
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
                NormalizedRelativePos.of(this.cameraPosition, chunk.sectionPos.asLong());
        chunk.chunkRenderData = new ChunkRenderData(normalizedRelativePos, renderData);
        uploadLayer(renderData.buffers, chunk);
    }

    private void uploadLayer(Map<BlockRenderLayer, BuiltBuffer> buffersByLayer, GhostBuiltChunk chunk) {
        buffersByLayer.forEach((layer, buffer) -> {
            ScopedProfiler scopedProfiler = Profilers.get().scoped("Upload Section Layer");

            try {
                chunk.chunkRenderData.upload(layer, buffer, chunk.sectionPos.asLong());
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

    /// @see net.minecraft.client.render.WorldRenderer
    public SectionRenderState renderBlockLayers(Matrix4fc matrix4fc, double d, double e, double f) {
        EnumMap<BlockRenderLayer, List<RenderPass.RenderObject<GpuBufferSlice[]>>> enumMap =
                new EnumMap<>(BlockRenderLayer.class);

        List<DynamicUniforms.UniformValue> list = new ArrayList<>();
        Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f matrix4f = new Matrix4f();

        for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
            enumMap.put(blockRenderLayer, new ArrayList<>());
        }

        int i = 0;
        for (var chunk : builtChunksTwinMap.values()) {
            for (BlockRenderLayer blockRenderLayer2 : BlockRenderLayer.values()) {
                Buffers buffers = chunk.chunkRenderData.getBuffersForLayer(blockRenderLayer2);
                if (buffers != null) {
                    GpuBuffer gpuBuffer;
                    VertexFormat.IndexType indexType;
                    if (buffers.getIndexBuffer() == null) {
                        if (buffers.getIndexCount() > i) {
                            i = buffers.getIndexCount();
                        }

                        gpuBuffer = null;
                        indexType = null;
                    } else {
                        gpuBuffer = buffers.getIndexBuffer();
                        indexType = buffers.getIndexType();
                    }

                    BlockPos blockPos = chunk.getOrigin();
                    int j = list.size();
                    list.add(
                            new DynamicUniforms.UniformValue(
                                    matrix4fc,
                                    vector4f,
                                    new Vector3f(
                                            (float)(blockPos.getX() - d),
                                            (float)(blockPos.getY() - e),
                                            (float)(blockPos.getZ() - f)),
                                    matrix4f,
                                    1.0F
                            )
                    );
                    RenderPass.RenderObject<GpuBufferSlice[]> dynamicTransforms = new RenderPass.RenderObject<>(
                            0,
                            buffers.getVertexBuffer(),
                            gpuBuffer,
                            indexType,
                            0,
                            buffers.getIndexCount(),
                            (gpuBufferSlicesx, uniformUploader) -> {
                                uniformUploader.upload(
                                        "DynamicTransforms",
                                        gpuBufferSlicesx[j]);
                            }
                    );
                    enumMap.get(blockRenderLayer2).add(dynamicTransforms);
                }
            }
        }
        GpuBufferSlice[] gpuBufferSlices = RenderSystem.getDynamicUniforms()
                .writeAll(list.toArray(new DynamicUniforms.UniformValue[0]));
        return new SectionRenderState(enumMap, i, gpuBufferSlices);
    }

    public static class GhostBuiltChunk {
        public ChunkRenderData chunkRenderData;
        public ChunkSectionPos sectionPos;
        public BlockPos.Mutable origin;

        public BlockPos getOrigin() {
            return this.origin;
        }
    }

}
