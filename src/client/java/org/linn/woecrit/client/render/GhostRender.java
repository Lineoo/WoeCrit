package org.linn.woecrit.client.render;

import net.minecraft.client.MinecraftClient;
import org.linn.woecrit.client.world.GhostWorld;

public class GhostRender {
    public GhostBlockRender blockRender;

    public GhostRender() {
        blockRender = new GhostBlockRender();
    }
}
