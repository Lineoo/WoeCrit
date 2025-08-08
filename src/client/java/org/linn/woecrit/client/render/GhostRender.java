package org.linn.woecrit.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.chunk.AbstractChunkRenderData;
import net.minecraft.client.render.chunk.Buffers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class GhostRender {
    public static final GhostRender INSTANCE = new GhostRender();

    private MinecraftClient client;
    public GhostBlockRender blockRender;

    private GhostRender() {
        this.client = MinecraftClient.getInstance();
        blockRender = new GhostBlockRender(client.getBlockRenderManager(), client.getBlockEntityRenderDispatcher());
        blockRender.build_new();
    }

    /// From [net.minecraft.client.render.WorldRenderer#renderBlockLayers]
    public SectionRenderState renderBlockLayers(Matrix4fc matrix4fc, double d, double e, double f) {
        AbstractChunkRenderData abstractChunkRenderData = blockRender.chunkRenderData;
        List<DynamicUniforms.UniformValue> list = new ArrayList();
        EnumMap<BlockRenderLayer, List<RenderPass.RenderObject<GpuBufferSlice[]>>> enumMap = new EnumMap(BlockRenderLayer.class);
        Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f matrix4f = new Matrix4f();

        for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
            enumMap.put(blockRenderLayer, new ArrayList());
        }

        int i = 0;
        for (BlockRenderLayer blockRenderLayer2 : BlockRenderLayer.values()) {
            Buffers buffers = abstractChunkRenderData.getBuffersForLayer(blockRenderLayer2);
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

                BlockPos blockPos = new BlockPos(0, -60, 0);
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
                RenderPass.RenderObject dynamicTransforms = new RenderPass.RenderObject(
                        0,
                        buffers.getVertexBuffer(),
                        gpuBuffer,
                        indexType,
                        0,
                        buffers.getIndexCount(),
                        (gpuBufferSlicesx, uniformUploader) -> {
                                ((RenderPass.UniformUploader)uniformUploader).upload(
                                        "DynamicTransforms",
                                        ((GpuBufferSlice[])gpuBufferSlicesx)[j]);
                        }
                );
                ((List)enumMap.get(blockRenderLayer2)).add(dynamicTransforms);
            }
        }

        GpuBufferSlice[] gpuBufferSlices = RenderSystem.getDynamicUniforms()
                .writeAll((DynamicUniforms.UniformValue[])list.toArray(new DynamicUniforms.UniformValue[0]));
        return new SectionRenderState(enumMap, i, gpuBufferSlices);
    }
}
