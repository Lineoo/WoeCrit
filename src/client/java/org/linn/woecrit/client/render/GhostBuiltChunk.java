package org.linn.woecrit.client.render;

import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public class GhostBuiltChunk {
    public ChunkRenderData chunkRenderData;
    public ChunkSectionPos sectionPos;
    public BlockPos.Mutable origin;

    public BlockPos getOrigin() {
        return this.origin;
    }
}
