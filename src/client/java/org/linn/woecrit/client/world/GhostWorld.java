package org.linn.woecrit.client.world;


import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import org.linn.woecrit.client.render.GhostRender;

import java.util.HashMap;
import java.util.Map;

/// @see net.minecraft.world.World
public class GhostWorld {
    public static final Map<RegistryEntry<DimensionType>, GhostWorld> worldsTwinMap = new HashMap<>();

    // Careful about memory leak.
    public final Map<ChunkPos, GhostChunk> chunksTwinMap = new HashMap<>();
    public final GhostRender render = new GhostRender();
}
